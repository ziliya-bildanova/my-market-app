package ru.yandex.practicum.mymarket.item.dto;

import java.util.List;

public record CatalogPageDto(List<List<ItemDto>> rows, PagingDto paging) {
}
