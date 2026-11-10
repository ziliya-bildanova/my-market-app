package ru.yandex.practicum.mymarket.cart.dto;

import java.util.List;
import ru.yandex.practicum.mymarket.item.dto.ItemDto;

public record CartDto(List<ItemDto> items, long total) {
}
