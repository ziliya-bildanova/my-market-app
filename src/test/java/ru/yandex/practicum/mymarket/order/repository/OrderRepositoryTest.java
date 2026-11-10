package ru.yandex.practicum.mymarket.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.item.model.Item;
import ru.yandex.practicum.mymarket.item.repository.ItemRepository;
import ru.yandex.practicum.mymarket.order.model.Order;
import ru.yandex.practicum.mymarket.order.model.OrderItem;
import ru.yandex.practicum.mymarket.support.AbstractDataJpaTest;

class OrderRepositoryTest extends AbstractDataJpaTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void orderIsSavedWithItemsAndPriceSnapshot() {
        Order saved = saveOrder("Мяч", 100, 2);
        flushAndClear();

        Order found = orderRepository.findWithItemsById(saved.getId()).orElseThrow();

        assertThat(Hibernate.isInitialized(found.getItems())).isTrue();
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().getFirst().getTitle()).isEqualTo("Мяч");
        assertThat(found.getTotalSum()).isEqualTo(200);
    }

    @Test
    void ordersAreLoadedInAscendingIdOrderWithItems() {
        Order first = saveOrder("Мяч", 100, 1);
        Order second = saveOrder("Палатка", 1000, 1);
        flushAndClear();

        List<Order> orders = orderRepository.findAllByOrderByIdAsc();

        assertThat(orders).extracting(Order::getId).containsExactly(first.getId(), second.getId());
        assertThat(orders).allSatisfy(order -> assertThat(Hibernate.isInitialized(order.getItems())).isTrue());
    }

    private Order saveOrder(String title, long price, int quantity) {
        Item item = itemRepository.save(new Item(title, "Описание", "images/a.svg", price));
        Order order = new Order(LocalDateTime.now());
        order.addItem(new OrderItem(item, quantity));
        return orderRepository.save(order);
    }
}
