package ru.yandex.practicum.mymarket.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.yandex.practicum.mymarket.cart.dto.CartAction;
import ru.yandex.practicum.mymarket.cart.dto.CartDto;
import ru.yandex.practicum.mymarket.cart.model.CartItem;
import ru.yandex.practicum.mymarket.cart.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.item.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.item.model.Item;
import ru.yandex.practicum.mymarket.item.repository.ItemRepository;
import ru.yandex.practicum.mymarket.support.TestItems;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartServiceTest {

    private static final long ITEM_ID = 1;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CartService cartService;

    private Item item;

    @BeforeEach
    void setUp() {
        item = TestItems.item(ITEM_ID, "Мяч", 100);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
    }

    @Test
    void plusCreatesCartItem() {
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.empty());

        cartService.changeItemCount(ITEM_ID, CartAction.PLUS);

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void plusIncreasesExistingCartItem() {
        CartItem cartItem = new CartItem(item);
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(cartItem));

        cartService.changeItemCount(ITEM_ID, CartAction.PLUS);

        assertThat(cartItem.getQuantity()).isEqualTo(2);
    }

    @Test
    void minusDecreasesQuantity() {
        CartItem cartItem = new CartItem(item);
        cartItem.increaseQuantity();
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(cartItem));

        cartService.changeItemCount(ITEM_ID, CartAction.MINUS);

        assertThat(cartItem.getQuantity()).isEqualTo(1);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void minusOnLastUnitRemovesCartItem() {
        CartItem cartItem = new CartItem(item);
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(cartItem));

        cartService.changeItemCount(ITEM_ID, CartAction.MINUS);

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void minusForItemOutsideCartDoesNothing() {
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.empty());

        cartService.changeItemCount(ITEM_ID, CartAction.MINUS);

        verify(cartItemRepository, never()).delete(any());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void deleteRemovesCartItemRegardlessOfQuantity() {
        CartItem cartItem = new CartItem(item);
        cartItem.increaseQuantity();
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.of(cartItem));

        cartService.changeItemCount(ITEM_ID, CartAction.DELETE);

        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void unknownItemThrows() {
        when(itemRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.changeItemCount(9, CartAction.PLUS))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void cartTotalIsSumOfPriceTimesCount() {
        CartItem first = new CartItem(item);
        first.increaseQuantity();
        CartItem second = new CartItem(TestItems.item(2, "Палатка", 1000));
        when(cartItemRepository.findAllByOrderByIdAsc()).thenReturn(List.of(first, second));

        CartDto cart = cartService.getCart();

        assertThat(cart.items()).hasSize(2);
        assertThat(cart.total()).isEqualTo(1200);
    }

    @Test
    void itemOutsideCartHasZeroCount() {
        when(cartItemRepository.findByItemId(ITEM_ID)).thenReturn(Optional.empty());

        assertThat(cartService.getItemCount(ITEM_ID)).isZero();
    }

    @Test
    void itemCountsAreMappedByItemId() {
        when(cartItemRepository.findByItemIdIn(List.of(ITEM_ID))).thenReturn(List.of(new CartItem(item)));

        assertThat(cartService.getItemCounts(List.of(ITEM_ID))).containsEntry(ITEM_ID, 1);
    }
}
