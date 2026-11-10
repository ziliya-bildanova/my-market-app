package ru.yandex.practicum.mymarket.item.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.item.dto.CatalogPageDto;
import ru.yandex.practicum.mymarket.item.dto.ItemDto;
import ru.yandex.practicum.mymarket.item.dto.PagingDto;
import ru.yandex.practicum.mymarket.item.dto.SortType;
import ru.yandex.practicum.mymarket.item.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.item.model.Item;
import ru.yandex.practicum.mymarket.item.repository.ItemRepository;

@Service
@Transactional(readOnly = true)
public class ItemService {

    private static final int ROW_SIZE = 3;
    private static final int MAX_PAGE_SIZE = 100;

    private final ItemRepository itemRepository;
    private final CartService cartService;

    public ItemService(ItemRepository itemRepository, CartService cartService) {
        this.itemRepository = itemRepository;
        this.cartService = cartService;
    }

    public CatalogPageDto getCatalogPage(String search, SortType sort, int pageNumber, int pageSize) {
        int number = Math.max(pageNumber, 1);
        int size = Math.clamp(pageSize, 1, MAX_PAGE_SIZE);
        Page<Item> page = findItems(search, PageRequest.of(number - 1, size, sort.toSort()));

        Map<Long, Integer> counts = cartService.getItemCounts(page.map(Item::getId).getContent());
        List<ItemDto> items = page.getContent().stream()
                .map(item -> ItemDto.of(item, counts.getOrDefault(item.getId(), 0)))
                .toList();

        return new CatalogPageDto(splitIntoRows(items),
                new PagingDto(size, number, page.hasPrevious(), page.hasNext()));
    }

    public ItemDto getItem(long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
        return ItemDto.of(item, cartService.getItemCount(id));
    }

    private Page<Item> findItems(String search, Pageable pageable) {
        if (!StringUtils.hasText(search)) {
            return itemRepository.findAll(pageable);
        }
        String text = search.strip();
        return itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(text, text, pageable);
    }

    private List<List<ItemDto>> splitIntoRows(List<ItemDto> items) {
        List<List<ItemDto>> rows = new ArrayList<>();
        for (int start = 0; start < items.size(); start += ROW_SIZE) {
            List<ItemDto> row = new ArrayList<>(items.subList(start, Math.min(start + ROW_SIZE, items.size())));
            while (row.size() < ROW_SIZE) {
                row.add(ItemDto.stub());
            }
            rows.add(List.copyOf(row));
        }
        return List.copyOf(rows);
    }
}
