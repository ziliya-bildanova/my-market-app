package ru.yandex.practicum.mymarket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import ru.yandex.practicum.mymarket.item.dto.ItemDto;
import ru.yandex.practicum.mymarket.item.model.Item;
import ru.yandex.practicum.mymarket.support.AbstractIntegrationTest;

class MarketFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void customerBuysItemsAndSeesOrder() throws Exception {
        Item ball = itemRepository.save(new Item("Мяч", "Футбольный", "images/ball.svg", 100));
        Item tent = itemRepository.save(new Item("Палатка", "Походная", "images/tent.svg", 1000));

        mockMvc.perform(post("/items").param("id", ball.getId().toString()).param("action", "PLUS"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/items/{id}", ball.getId()).param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("item", ItemDto.of(ball, 2)));
        mockMvc.perform(post("/cart/items").param("id", tent.getId().toString()).param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("total", 1200L))
                .andExpect(content().string(containsString("Итого: 1200 руб.")));

        String location = mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        assertThat(location).matches("/orders/\\d+\\?newOrder=true");
        assertThat(cartItemRepository.count()).isZero();

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("newOrder", true))
                .andExpect(content().string(containsString("Поздравляем")))
                .andExpect(content().string(containsString("Сумма: 1200 руб.")));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("orders", hasSize(1)))
                .andExpect(content().string(containsString("Мяч (2 шт.) 200 руб.")));
    }

    @Test
    void catalogIsSearchedSortedAndPaged() throws Exception {
        itemRepository.save(new Item("Яблоко", "Фрукт", "images/a.svg", 30));
        itemRepository.save(new Item("Арбуз", "Ягода", "images/b.svg", 20));
        itemRepository.save(new Item("Банан", "Фрукт", "images/c.svg", 10));

        mockMvc.perform(get("/items").param("search", "фрукт").param("sort", "PRICE").param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Банан")))
                .andExpect(content().string(not(containsString("Яблоко"))))
                .andExpect(content().string(not(containsString("Арбуз"))));

        mockMvc.perform(get("/").param("sort", "ALPHA").param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Арбуз")));
    }

    @Test
    void importedItemsAppearInCatalog() throws Exception {
        String csv = "title;description;imgPath;price\nГантели;Набор;;4900\n";
        MockMultipartFile file = new MockMultipartFile("file", "items.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/items/import").file(file))
                .andExpect(redirectedUrl("/items/import"));

        mockMvc.perform(get("/items"))
                .andExpect(content().string(containsString("Гантели")))
                .andExpect(content().string(containsString("/images/no-image.svg")));
    }

    @Test
    void bundledImageIsServed() throws Exception {
        mockMvc.perform(get("/images/ball.svg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/svg+xml"));
    }
}
