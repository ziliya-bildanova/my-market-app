package ru.yandex.practicum.mymarket.purchase.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.item.repository.ItemRepository;
import ru.yandex.practicum.mymarket.order.model.Order;
import ru.yandex.practicum.mymarket.order.model.OrderItem;
import ru.yandex.practicum.mymarket.order.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.order.repository.OrderRepository;

@Service
public class PurchaseService {
    private final CartItemRepository carts;
    private final ItemRepository items;
    private final OrderRepository orders;
    private final OrderItemRepository lines;
    public PurchaseService(CartItemRepository carts, ItemRepository items, OrderRepository orders, OrderItemRepository lines) {
        this.carts = carts; this.items = items; this.orders = orders; this.lines = lines;
    }
    @Transactional
    public Mono<Long> purchase() {
        return carts.findAllByOrderByIdAsc().collectList().flatMap(cart -> {
            if (cart.isEmpty()) return Mono.empty();
            return orders.save(new Order(LocalDateTime.now())).flatMap(order ->
                reactor.core.publisher.Flux.fromIterable(cart).concatMap(line -> items.findById(line.getItemId())
                    .flatMap(item -> lines.save(new OrderItem(order.getId(), item.getId(), item.getTitle(),
                            item.getPrice(), line.getQuantity())))).then(carts.deleteAll(cart))
                    .thenReturn(order.getId()));
        });
    }
}
