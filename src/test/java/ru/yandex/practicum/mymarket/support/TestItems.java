package ru.yandex.practicum.mymarket.support;

import org.springframework.test.util.ReflectionTestUtils;
import ru.yandex.practicum.mymarket.item.dto.ItemDto;
import ru.yandex.practicum.mymarket.item.model.Item;

public final class TestItems {

    private TestItems() {
    }

    public static Item item(long id, String title, long price) {
        Item item = new Item(title, title + " описание", "images/" + id + ".svg", price);
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    public static ItemDto itemDto(long id, String title, long price, int count) {
        return new ItemDto(id, title, title + " описание", "images/" + id + ".svg", price, count);
    }
}
