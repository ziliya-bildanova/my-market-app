package ru.yandex.practicum.mymarket.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.yandex.practicum.mymarket.item.dto.ItemDto;
import ru.yandex.practicum.mymarket.order.dto.OrderDto;
import ru.yandex.practicum.mymarket.order.exception.OrderNotFoundException;
import ru.yandex.practicum.mymarket.order.model.Order;
import ru.yandex.practicum.mymarket.order.model.OrderItem;
import ru.yandex.practicum.mymarket.order.repository.OrderRepository;
import ru.yandex.practicum.mymarket.support.TestItems;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void orderIsMappedWithTotalSum() {
        when(orderRepository.findWithItemsById(7L)).thenReturn(Optional.of(order()));

        OrderDto dto = orderService.getOrder(7);

        assertThat(dto.id()).isEqualTo(7);
        assertThat(dto.totalSum()).isEqualTo(1200);
        assertThat(dto.items()).containsExactly(
                new ItemDto(1, "Мяч", null, null, 100, 2),
                new ItemDto(2, "Палатка", null, null, 1000, 1));
    }

    @Test
    void ordersAreMapped() {
        when(orderRepository.findAllByOrderByIdAsc()).thenReturn(List.of(order()));

        assertThat(orderService.getOrders()).extracting(OrderDto::id).containsExactly(7L);
    }

    @Test
    void missingOrderThrows() {
        when(orderRepository.findWithItemsById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(9)).isInstanceOf(OrderNotFoundException.class);
    }

    private static Order order() {
        Order order = new Order(LocalDateTime.now());
        ReflectionTestUtils.setField(order, "id", 7L);
        order.addItem(new OrderItem(TestItems.item(1, "Мяч", 100), 2));
        order.addItem(new OrderItem(TestItems.item(2, "Палатка", 1000), 1));
        return order;
    }
}
