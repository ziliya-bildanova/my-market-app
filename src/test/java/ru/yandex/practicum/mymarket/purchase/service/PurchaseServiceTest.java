package ru.yandex.practicum.mymarket.purchase.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.yandex.practicum.mymarket.cart.model.CartItem;
import ru.yandex.practicum.mymarket.cart.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.order.model.Order;
import ru.yandex.practicum.mymarket.order.repository.OrderRepository;
import ru.yandex.practicum.mymarket.support.TestItems;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    void emptyCartCreatesNoOrder() {
        when(cartItemRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        assertThat(purchaseService.purchase()).isEmpty();
        verify(orderRepository, never()).save(any());
    }

    @Test
    void purchaseCreatesOrderAndClearsCart() {
        CartItem cartItem = new CartItem(TestItems.item(1, "Мяч", 100));
        cartItem.increaseQuantity();
        List<CartItem> cartItems = List.of(cartItem);
        when(cartItemRepository.findAllByOrderByIdAsc()).thenReturn(cartItems);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 7L);
            return order;
        });

        assertThat(purchaseService.purchase()).contains(7L);

        ArgumentCaptor<Order> order = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(order.capture());
        assertThat(order.getValue().getTotalSum()).isEqualTo(200);
        assertThat(order.getValue().getItems()).hasSize(1);
        verify(cartItemRepository).deleteAll(cartItems);
    }
}
