package ru.yandex.practicum.mymarket.purchase.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.cart.model.CartItem;
import ru.yandex.practicum.mymarket.cart.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.order.model.Order;
import ru.yandex.practicum.mymarket.order.model.OrderItem;
import ru.yandex.practicum.mymarket.order.repository.OrderRepository;

@Service
public class PurchaseService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;

    public PurchaseService(CartItemRepository cartItemRepository, OrderRepository orderRepository) {
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Эмулирует покупку: создаёт заказ из корзины и очищает её.
     *
     * @return идентификатор заказа или пустой результат, если корзина пуста
     */
    @Transactional
    public Optional<Long> purchase() {
        List<CartItem> cartItems = cartItemRepository.findAllByOrderByIdAsc();
        if (cartItems.isEmpty()) {
            return Optional.empty();
        }
        Order order = new Order(LocalDateTime.now());
        cartItems.forEach(cartItem -> order.addItem(new OrderItem(cartItem.getItem(), cartItem.getQuantity())));
        Order saved = orderRepository.save(order);
        cartItemRepository.deleteAll(cartItems);
        return Optional.of(saved.getId());
    }
}
