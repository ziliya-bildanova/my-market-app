package ru.yandex.practicum.mymarket.order.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.order.dto.OrderDto;
import ru.yandex.practicum.mymarket.order.exception.OrderNotFoundException;
import ru.yandex.practicum.mymarket.order.repository.OrderRepository;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderDto> getOrders() {
        return orderRepository.findAllByOrderByIdAsc().stream().map(OrderDto::of).toList();
    }

    public OrderDto getOrder(long id) {
        return orderRepository.findWithItemsById(id)
                .map(OrderDto::of)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
