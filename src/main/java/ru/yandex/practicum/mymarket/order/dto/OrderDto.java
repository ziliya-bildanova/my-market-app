package ru.yandex.practicum.mymarket.order.dto;

import java.util.List;
import ru.yandex.practicum.mymarket.item.dto.ItemDto;
import ru.yandex.practicum.mymarket.order.model.OrderItem;

public record OrderDto(long id, List<ItemDto> items, long totalSum) {
    public static OrderDto of(long id, List<OrderItem> lines) {
        List<ItemDto> items = lines.stream().map(line -> new ItemDto(line.getItemId(), line.getTitle(), null,
                null, line.getPrice(), line.getQuantity())).toList();
        return new OrderDto(id, items, items.stream().mapToLong(item -> item.price() * item.count()).sum());
    }
}
