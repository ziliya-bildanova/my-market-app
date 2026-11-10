package ru.yandex.practicum.mymarket.item.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import ru.yandex.practicum.mymarket.item.exception.ItemImportException;
import ru.yandex.practicum.mymarket.support.AbstractWebMvcTest;

class ItemImportControllerTest extends AbstractWebMvcTest {

    private static final MockMultipartFile FILE =
            new MockMultipartFile("file", "items.csv", "text/csv", "data".getBytes());

    @Test
    void importPageIsShown() throws Exception {
        mockMvc.perform(get("/items/import"))
                .andExpect(status().isOk())
                .andExpect(view().name("items-import"));
    }

    @Test
    void successfulImportReportsCount() throws Exception {
        when(itemImportService.importItems(any())).thenReturn(3);

        mockMvc.perform(multipart("/items/import").file(FILE))
                .andExpect(redirectedUrl("/items/import"))
                .andExpect(flash().attribute("imported", 3));
    }

    @Test
    void failedImportReportsError() throws Exception {
        when(itemImportService.importItems(any())).thenThrow(new ItemImportException("Файл пустой"));

        mockMvc.perform(multipart("/items/import").file(FILE))
                .andExpect(redirectedUrl("/items/import"))
                .andExpect(flash().attribute("error", "Файл пустой"));
    }
}
