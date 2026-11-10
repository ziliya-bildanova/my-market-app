package ru.yandex.practicum.mymarket.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mymarket.item.exception.ItemImportException;
import ru.yandex.practicum.mymarket.item.model.Item;
import ru.yandex.practicum.mymarket.item.repository.ItemRepository;

@ExtendWith(MockitoExtension.class)
class ItemImportServiceTest {

    private static final String HEADER = "title;description;imgPath;price\n";

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemImportService itemImportService;

    @Captor
    private ArgumentCaptor<List<Item>> itemsCaptor;

    @Test
    void validLinesAreSaved() {
        int imported = itemImportService.importItems(csv(HEADER + "Мяч;Футбольный;/images/ball.svg;100\n\nГантели;Набор;;50\n"));

        assertThat(imported).isEqualTo(2);
        verify(itemRepository).saveAll(itemsCaptor.capture());
        Item first = itemsCaptor.getValue().getFirst();
        assertThat(first.getTitle()).isEqualTo("Мяч");
        assertThat(first.getImgPath()).isEqualTo("images/ball.svg");
        assertThat(first.getPrice()).isEqualTo(100);
        assertThat(itemsCaptor.getValue().get(1).getImgPath()).isEqualTo("images/no-image.svg");
    }

    @Test
    void emptyFileIsRejected() {
        assertThatThrownBy(() -> itemImportService.importItems(csv("")))
                .isInstanceOf(ItemImportException.class)
                .hasMessage("Файл пустой");
    }

    @Test
    void wrongColumnCountIsReportedWithLineNumber() {
        assertThatThrownBy(() -> itemImportService.importItems(csv(HEADER + "Мяч;100\n")))
                .isInstanceOf(ItemImportException.class)
                .hasMessageContaining("Строка 2");
    }

    @Test
    void invalidPriceRejectsWholeFile() {
        assertThatThrownBy(() -> itemImportService.importItems(csv(HEADER + "Мяч;;;100\nГантели;;;дорого\n")))
                .isInstanceOf(ItemImportException.class)
                .hasMessageContaining("Строка 3");
        verify(itemRepository, never()).saveAll(any());
    }

    @Test
    void negativePriceIsRejected() {
        assertThatThrownBy(() -> itemImportService.importItems(csv(HEADER + "Мяч;;;-1\n")))
                .isInstanceOf(ItemImportException.class)
                .hasMessageContaining("отрицательной");
    }

    @Test
    void blankTitleIsRejected() {
        assertThatThrownBy(() -> itemImportService.importItems(csv(HEADER + " ;;;1\n")))
                .isInstanceOf(ItemImportException.class)
                .hasMessageContaining("название");
    }

    private static InputStream csv(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
