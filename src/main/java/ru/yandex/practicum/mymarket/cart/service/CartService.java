package ru.yandex.practicum.mymarket.cart.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.dto.CartAction;
import ru.yandex.practicum.mymarket.cart.dto.CartDto;
import ru.yandex.practicum.mymarket.cart.model.CartItem;
import ru.yandex.practicum.mymarket.cart.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.item.dto.ItemDto;
import ru.yandex.practicum.mymarket.item.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.item.repository.ItemRepository;

@Service
public class CartService {
    private final CartItemRepository carts;
    private final ItemRepository items;
    public CartService(CartItemRepository carts, ItemRepository items) { this.carts = carts; this.items = items; }

    public Mono<CartDto> getCart() {
        return carts.findAllByOrderByIdAsc().collectList().flatMap(lines ->
            items.findAllById(lines.stream().map(CartItem::getItemId).toList()).collectMap(item -> item.getId())
                .map(byId -> {
                    List<ItemDto> result = lines.stream().map(line -> ItemDto.of(byId.get(line.getItemId()), line.getQuantity())).toList();
                    return new CartDto(result, result.stream().mapToLong(item -> item.price() * item.count()).sum());
                }));
    }

    public Mono<Map<Long, Integer>> getItemCounts(List<Long> ids) {
        if (ids.isEmpty()) return Mono.just(Map.of());
        return carts.findAll().filter(line -> ids.contains(line.getItemId()))
                .collectMap(CartItem::getItemId, CartItem::getQuantity);
    }

    public Mono<Integer> getItemCount(long id) {
        return carts.findByItemId(id).map(CartItem::getQuantity).defaultIfEmpty(0);
    }

    @Transactional
    public Mono<Void> changeItemCount(long id, CartAction action) {
        return items.findById(id).switchIfEmpty(Mono.error(new ItemNotFoundException(id)))
            .flatMap(item -> carts.findByItemId(id).flatMap(line -> {
                if (action == CartAction.DELETE || (action == CartAction.MINUS && line.getQuantity() == 1))
                    return carts.delete(line).thenReturn(line);
                line.setQuantity(line.getQuantity() + (action == CartAction.PLUS ? 1 : -1));
                return carts.save(line);
            }).switchIfEmpty(Mono.defer(() -> action == CartAction.PLUS
                    ? carts.save(new CartItem(id, 1)) : Mono.empty()))).then();
    }
}
