package ru.yandex.practicum.mymarket.order.dto;

import java.util.List;
import ru.yandex.practicum.mymarket.item.dto.ItemDto;
import ru.yandex.practicum.mymarket.order.model.Order;
import ru.yandex.practicum.mymarket.order.model.OrderItem;

public record OrderDto(long id, List<ItemDto> items, long totalSum) {

    public static OrderDto of(Order order) {
        List<ItemDto> items = order.getItems().stream().map(OrderDto::toItemDto).toList();
        return new OrderDto(order.getId(), items, order.getTotalSum());
    }

    private static ItemDto toItemDto(OrderItem orderItem) {
        return new ItemDto(orderItem.getItem().getId(), orderItem.getTitle(), null, null,
                orderItem.getPrice(), orderItem.getQuantity());
    }
}
