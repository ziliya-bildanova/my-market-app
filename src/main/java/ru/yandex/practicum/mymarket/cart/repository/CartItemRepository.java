package ru.yandex.practicum.mymarket.cart.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.model.CartItem;

public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {
    Mono<CartItem> findByItemId(Long itemId);
    Flux<CartItem> findAllByOrderByIdAsc();
    Mono<Void> deleteByItemId(Long itemId);
}
