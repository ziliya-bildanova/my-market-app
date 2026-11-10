package ru.yandex.practicum.mymarket.item.dto;

public record PagingDto(int pageSize, int pageNumber, boolean hasPrevious, boolean hasNext) {
}
