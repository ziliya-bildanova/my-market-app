package ru.yandex.practicum.mymarket.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.item.dto.CatalogPageDto;
import ru.yandex.practicum.mymarket.item.dto.ItemDto;
import ru.yandex.practicum.mymarket.item.dto.SortType;
import ru.yandex.practicum.mymarket.item.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.item.model.Item;
import ru.yandex.practicum.mymarket.item.repository.ItemRepository;
import ru.yandex.practicum.mymarket.support.TestItems;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private ItemService itemService;

    private List<Item> items;

    @BeforeEach
    void setUp() {
        items = LongStream.rangeClosed(1, 5).mapToObj(id -> TestItems.item(id, "Товар " + id, id * 10)).toList();
    }

    @Test
    void itemsAreSplitIntoRowsOfThreeWithStubs() {
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(items));
        when(cartService.getItemCounts(anyCollection())).thenReturn(Map.of(2L, 4));

        CatalogPageDto page = itemService.getCatalogPage("", SortType.NO, 1, 5);

        assertThat(page.rows()).hasSize(2).allSatisfy(row -> assertThat(row).hasSize(3));
        assertThat(page.rows().get(0).get(1).count()).isEqualTo(4);
        assertThat(page.rows().get(1).get(2)).isEqualTo(ItemDto.stub());
    }

    @Test
    void pagingReflectsPosition() {
        Pageable request = PageRequest.of(1, 2, SortType.PRICE.toSort());
        when(itemRepository.findAll(request)).thenReturn(new PageImpl<>(items.subList(2, 4), request, 5));

        CatalogPageDto page = itemService.getCatalogPage(null, SortType.PRICE, 2, 2);

        assertThat(page.paging().pageNumber()).isEqualTo(2);
        assertThat(page.paging().pageSize()).isEqualTo(2);
        assertThat(page.paging().hasPrevious()).isTrue();
        assertThat(page.paging().hasNext()).isTrue();
    }

    @Test
    void invalidPagingValuesAreNormalized() {
        Pageable request = PageRequest.of(0, 100, SortType.NO.toSort());
        when(itemRepository.findAll(request)).thenReturn(new PageImpl<>(List.of(), request, 0));

        CatalogPageDto page = itemService.getCatalogPage("", SortType.NO, -3, 1000);

        assertThat(page.paging().pageNumber()).isEqualTo(1);
        assertThat(page.paging().pageSize()).isEqualTo(100);
    }

    @Test
    void searchTextIsStrippedAndAppliedToTitleAndDescription() {
        Pageable request = PageRequest.of(0, 5, SortType.ALPHA.toSort());
        when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("мяч", "мяч", request))
                .thenReturn(new PageImpl<>(List.of(), request, 0));

        itemService.getCatalogPage("  мяч ", SortType.ALPHA, 1, 5);

        verify(itemRepository)
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("мяч", "мяч", request);
    }

    @Test
    void itemIncludesCartCount() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(items.getFirst()));
        when(cartService.getItemCount(1)).thenReturn(3);

        assertThat(itemService.getItem(1).count()).isEqualTo(3);
    }

    @Test
    void missingItemThrows() {
        when(itemRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItem(9)).isInstanceOf(ItemNotFoundException.class);
    }
}
