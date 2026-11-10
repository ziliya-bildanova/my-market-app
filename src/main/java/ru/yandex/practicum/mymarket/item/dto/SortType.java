package ru.yandex.practicum.mymarket.item.dto;

import org.springframework.data.domain.Sort;

public enum SortType {

    NO(Sort.by("id")),
    ALPHA(Sort.by(Sort.Order.asc("title").ignoreCase(), Sort.Order.asc("id"))),
    PRICE(Sort.by("price", "id"));

    private final Sort sort;

    SortType(Sort sort) {
        this.sort = sort;
    }

    /** Порядок всегда включает id: без него страницы могут пересекаться. */
    public Sort toSort() {
        return sort;
    }
}
