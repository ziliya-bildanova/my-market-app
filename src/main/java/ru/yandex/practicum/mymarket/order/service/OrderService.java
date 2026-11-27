package ru.yandex.practicum.mymarket.order.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.order.dto.OrderDto;
import ru.yandex.practicum.mymarket.order.exception.OrderNotFoundException;
import ru.yandex.practicum.mymarket.order.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.order.repository.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final OrderItemRepository lines;
    public OrderService(OrderRepository orders, OrderItemRepository lines) { this.orders = orders; this.lines = lines; }
    public Flux<OrderDto> getOrders() { return orders.findAllByOrderByIdDesc().concatMap(order -> getOrder(order.getId())); }
    public Mono<OrderDto> getOrder(long id) {
        return orders.findById(id).switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
                .flatMap(order -> lines.findByOrderIdOrderByIdAsc(id).collectList().map(found -> OrderDto.of(id, found)));
    }
}
