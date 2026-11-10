package ru.yandex.practicum.mymarket.cart.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import ru.yandex.practicum.mymarket.cart.model.CartItem;
import ru.yandex.practicum.mymarket.item.model.Item;
import ru.yandex.practicum.mymarket.item.repository.ItemRepository;
import ru.yandex.practicum.mymarket.support.AbstractDataJpaTest;

class CartItemRepositoryTest extends AbstractDataJpaTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findsByItemId() {
        Item item = saveItem("Мяч");
        cartItemRepository.save(new CartItem(item));
        flushAndClear();

        assertThat(cartItemRepository.findByItemId(item.getId())).isPresent();
        assertThat(cartItemRepository.findByItemId(-1L)).isEmpty();
    }

    @Test
    void findsOnlyRequestedItems() {
        Item first = saveItem("Мяч");
        Item second = saveItem("Палатка");
        cartItemRepository.save(new CartItem(first));
        cartItemRepository.save(new CartItem(second));
        flushAndClear();

        assertThat(cartItemRepository.findByItemIdIn(List.of(second.getId()))).hasSize(1);
    }

    @Test
    void allCartItemsAreLoadedWithItems() {
        cartItemRepository.save(new CartItem(saveItem("Мяч")));
        flushAndClear();

        List<CartItem> cartItems = cartItemRepository.findAllByOrderByIdAsc();

        assertThat(cartItems).hasSize(1);
        assertThat(Hibernate.isInitialized(cartItems.getFirst().getItem())).isTrue();
    }

    @Test
    void itemCanBeInCartOnlyOnce() {
        Item item = saveItem("Мяч");
        cartItemRepository.saveAndFlush(new CartItem(item));

        assertThatThrownBy(() -> cartItemRepository.saveAndFlush(new CartItem(item)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Item saveItem(String title) {
        return itemRepository.save(new Item(title, "Описание", "images/a.svg", 100));
    }
}
