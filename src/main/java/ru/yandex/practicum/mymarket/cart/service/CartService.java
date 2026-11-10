package ru.yandex.practicum.mymarket.cart.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.cart.dto.CartAction;
import ru.yandex.practicum.mymarket.cart.dto.CartDto;
import ru.yandex.practicum.mymarket.cart.model.CartItem;
import ru.yandex.practicum.mymarket.cart.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.item.dto.ItemDto;
import ru.yandex.practicum.mymarket.item.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.item.model.Item;
import ru.yandex.practicum.mymarket.item.repository.ItemRepository;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

    public CartService(CartItemRepository cartItemRepository, ItemRepository itemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.itemRepository = itemRepository;
    }

    public CartDto getCart() {
        List<ItemDto> items = cartItemRepository.findAllByOrderByIdAsc().stream()
                .map(cartItem -> ItemDto.of(cartItem.getItem(), cartItem.getQuantity()))
                .toList();
        long total = items.stream().mapToLong(item -> item.price() * item.count()).sum();
        return new CartDto(items, total);
    }

    public Map<Long, Integer> getItemCounts(Collection<Long> itemIds) {
        return cartItemRepository.findByItemIdIn(itemIds).stream()
                .collect(Collectors.toMap(cartItem -> cartItem.getItem().getId(), CartItem::getQuantity));
    }

    public int getItemCount(long itemId) {
        return cartItemRepository.findByItemId(itemId).map(CartItem::getQuantity).orElse(0);
    }

    @Transactional
    public void changeItemCount(long itemId, CartAction action) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException(itemId));
        switch (action) {
            case PLUS -> increase(item);
            case MINUS -> decrease(itemId);
            case DELETE -> cartItemRepository.findByItemId(itemId).ifPresent(cartItemRepository::delete);
        }
    }

    private void increase(Item item) {
        cartItemRepository.findByItemId(item.getId()).ifPresentOrElse(
                CartItem::increaseQuantity,
                () -> cartItemRepository.save(new CartItem(item)));
    }

    private void decrease(long itemId) {
        cartItemRepository.findByItemId(itemId).ifPresent(cartItem -> {
            if (cartItem.isLastUnit()) {
                cartItemRepository.delete(cartItem);
            } else {
                cartItem.decreaseQuantity();
            }
        });
    }
}
