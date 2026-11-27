package ru.yandex.practicum.mymarket.item.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.item.dto.CatalogPageDto;
import ru.yandex.practicum.mymarket.item.dto.ItemDto;
import ru.yandex.practicum.mymarket.item.dto.PagingDto;
import ru.yandex.practicum.mymarket.item.dto.SortType;
import ru.yandex.practicum.mymarket.item.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.item.model.Item;
import ru.yandex.practicum.mymarket.item.repository.ItemRepository;

@Service
public class ItemService {
    private final ItemRepository items;
    private final CartService cart;
    public ItemService(ItemRepository items, CartService cart) { this.items = items; this.cart = cart; }

    public Mono<CatalogPageDto> getCatalogPage(String search, SortType sort, int pageNumber, int pageSize) {
        int number = Math.max(pageNumber, 1);
        int size = List.of(2, 5, 10, 20, 50, 100).contains(pageSize) ? pageSize : 5;
        Flux<Item> source = StringUtils.hasText(search) ? items.search("%" + search.strip().toLowerCase()
                .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%") : items.findAll();
        Comparator<Item> comparator = switch (sort) {
            case ALPHA -> Comparator.comparing(Item::getTitle, String.CASE_INSENSITIVE_ORDER).thenComparing(Item::getId);
            case PRICE -> Comparator.comparingLong(Item::getPrice).thenComparing(Item::getId);
            case NO -> Comparator.comparing(Item::getId);
        };
        return source.sort(comparator).skip((long) (number - 1) * size).take(size + 1L).collectList()
            .flatMap(found -> {
                boolean next = found.size() > size;
                List<Item> page = found.subList(0, Math.min(size, found.size()));
                return cart.getItemCounts(page.stream().map(Item::getId).toList()).map(counts -> {
                    List<ItemDto> entries = page.stream().map(item -> ItemDto.of(item, counts.getOrDefault(item.getId(), 0))).toList();
                    List<List<ItemDto>> rows = new ArrayList<>();
                    for (int start = 0; start < entries.size(); start += 3) {
                        List<ItemDto> row = new ArrayList<>(entries.subList(start, Math.min(start + 3, entries.size())));
                        while (row.size() < 3) row.add(ItemDto.stub());
                        rows.add(List.copyOf(row));
                    }
                    return new CatalogPageDto(rows, new PagingDto(size, number, number > 1, next));
                });
            });
    }

    public Mono<ItemDto> getItem(long id) {
        return items.findById(id).switchIfEmpty(Mono.error(new ItemNotFoundException(id)))
                .flatMap(item -> cart.getItemCount(id).map(count -> ItemDto.of(item, count)));
    }
}
