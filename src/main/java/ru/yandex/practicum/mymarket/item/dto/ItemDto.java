package ru.yandex.practicum.mymarket.item.dto;

import ru.yandex.practicum.mymarket.item.model.Item;

public record ItemDto(long id, String title, String description, String imgPath, long price, int count) {

    private static final long STUB_ID = -1;

    public static ItemDto of(Item item, int count) {
        return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(),
                item.getPrice(), count);
    }

    /** Заглушка для пустой ячейки в неполной строке витрины. */
    public static ItemDto stub() {
        return new ItemDto(STUB_ID, "", "", "", 0, 0);
    }
}
