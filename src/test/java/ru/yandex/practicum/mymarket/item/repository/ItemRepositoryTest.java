package ru.yandex.practicum.mymarket.item.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.mymarket.item.dto.SortType;
import ru.yandex.practicum.mymarket.item.model.Item;
import ru.yandex.practicum.mymarket.support.AbstractDataJpaTest;

class ItemRepositoryTest extends AbstractDataJpaTest {

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        itemRepository.save(new Item("Мяч футбольный", "Для игры", "images/a.svg", 300));
        itemRepository.save(new Item("Палатка", "Брать в поход", "images/b.svg", 100));
        itemRepository.save(new Item("арбуз", "Скидка 100% на МЯЧ", "images/c.svg", 200));
        flushAndClear();
    }

    @Test
    void searchMatchesTitleOrDescriptionIgnoringCase() {
        assertThat(search("мЯч", SortType.NO, 10)).extracting(Item::getTitle)
                .containsExactly("Мяч футбольный", "арбуз");
    }

    @Test
    void likeWildcardsAreSearchedLiterally() {
        assertThat(search("100%", SortType.NO, 10)).extracting(Item::getTitle).containsExactly("арбуз");
        assertThat(search("%", SortType.NO, 10)).extracting(Item::getTitle).containsExactly("арбуз");
    }

    @Test
    void alphaSortIgnoresCase() {
        assertThat(itemRepository.findAll(PageRequest.of(0, 10, SortType.ALPHA.toSort())))
                .extracting(Item::getTitle)
                .containsExactly("арбуз", "Мяч футбольный", "Палатка");
    }

    @Test
    void priceSortIsAscending() {
        assertThat(itemRepository.findAll(PageRequest.of(0, 10, SortType.PRICE.toSort())))
                .extracting(Item::getPrice)
                .containsExactly(100L, 200L, 300L);
    }

    @Test
    void pagesReportNeighbours() {
        Page<Item> first = itemRepository.findAll(PageRequest.of(0, 2, SortType.NO.toSort()));
        Page<Item> last = itemRepository.findAll(PageRequest.of(1, 2, SortType.NO.toSort()));

        assertThat(first.hasNext()).isTrue();
        assertThat(first.hasPrevious()).isFalse();
        assertThat(last.hasNext()).isFalse();
        assertThat(last.hasPrevious()).isTrue();
    }

    private Page<Item> search(String text, SortType sort, int size) {
        Pageable pageable = PageRequest.of(0, size, sort.toSort());
        return itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(text, text, pageable);
    }
}
