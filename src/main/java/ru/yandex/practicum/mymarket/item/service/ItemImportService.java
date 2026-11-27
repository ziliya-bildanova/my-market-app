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
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.item.exception.ItemImportException;
import ru.yandex.practicum.mymarket.item.model.Item;
import ru.yandex.practicum.mymarket.item.repository.ItemRepository;

@Service
public class ItemImportService {
    private final ItemRepository items;
    public ItemImportService(ItemRepository items) { this.items = items; }

    @Transactional
    public Mono<Integer> importItems(InputStream csv) {
        return Mono.fromCallable(() -> parse(csv)).flatMap(parsed -> items.saveAll(parsed).count().map(Long::intValue));
    }

    private List<Item> parse(InputStream csv) {
        List<Item> parsed = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csv, StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null || header.replace("\uFEFF", "").isBlank()) throw new ItemImportException("Файл пустой");
            String line;
            int number = 1;
            while ((line = reader.readLine()) != null) {
                number++;
                if (line.isBlank()) continue;
                String[] columns = line.split(";", -1);
                if (columns.length != 4 || columns[0].isBlank()) throw new ItemImportException("Строка " + number + ": неверный формат");
                long price;
                try { price = Long.parseLong(columns[3].strip()); }
                catch (NumberFormatException ex) { throw new ItemImportException("Строка " + number + ": некорректная цена"); }
                if (price < 0) throw new ItemImportException("Строка " + number + ": цена не может быть отрицательной");
                String image = columns[2].strip().replaceFirst("^/", "");
                parsed.add(new Item(columns[0].strip(), columns[1].strip(), image.isEmpty() ? "images/no-image.svg" : image, price));
            }
        } catch (IOException ex) { throw new ItemImportException("Не удалось прочитать файл", ex); }
        return parsed;
    }
}
