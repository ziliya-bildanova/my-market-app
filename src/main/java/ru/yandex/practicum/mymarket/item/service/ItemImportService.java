package ru.yandex.practicum.mymarket.item.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.mymarket.item.exception.ItemImportException;
import ru.yandex.practicum.mymarket.item.model.Item;
import ru.yandex.practicum.mymarket.item.repository.ItemRepository;

/**
 * Загружает товары из CSV в UTF-8 с разделителем «;» и строкой заголовка:
 * {@code title;description;imgPath;price}. Файл загружается целиком или не загружается совсем.
 */
@Service
public class ItemImportService {

    private static final String SEPARATOR = ";";
    private static final int COLUMNS = 4;
    private static final String BYTE_ORDER_MARK = "﻿";
    private static final String DEFAULT_IMAGE = "images/no-image.svg";

    private final ItemRepository itemRepository;

    public ItemImportService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional
    public int importItems(InputStream csv) {
        List<Item> items = parse(csv);
        itemRepository.saveAll(items);
        return items.size();
    }

    private List<Item> parse(InputStream csv) {
        List<Item> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csv, StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null || header.replace(BYTE_ORDER_MARK, "").isBlank()) {
                throw new ItemImportException("Файл пустой");
            }
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (!line.isBlank()) {
                    items.add(parseLine(line, lineNumber));
                }
            }
        } catch (IOException e) {
            throw new ItemImportException("Не удалось прочитать файл", e);
        }
        return items;
    }

    private Item parseLine(String line, int lineNumber) {
        String[] columns = line.split(SEPARATOR, -1);
        if (columns.length != COLUMNS) {
            throw new ItemImportException("Строка " + lineNumber + ": ожидается колонок: " + COLUMNS);
        }
        String title = columns[0].strip();
        if (title.isEmpty()) {
            throw new ItemImportException("Строка " + lineNumber + ": не указано название");
        }
        return new Item(title, columns[1].strip(), normalizeImagePath(columns[2]), parsePrice(columns[3], lineNumber));
    }

    private String normalizeImagePath(String column) {
        String path = StringUtils.trimLeadingCharacter(column.strip(), '/');
        return path.isEmpty() ? DEFAULT_IMAGE : path;
    }

    private long parsePrice(String column, int lineNumber) {
        try {
            long price = Long.parseLong(column.strip());
            if (price < 0) {
                throw new ItemImportException("Строка " + lineNumber + ": цена не может быть отрицательной");
            }
            return price;
        } catch (NumberFormatException e) {
            throw new ItemImportException("Строка " + lineNumber + ": некорректная цена", e);
        }
    }
}
