package ru.yandex.practicum.mymarket.item.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.mymarket.cart.dto.CartAction;
import ru.yandex.practicum.mymarket.item.dto.CatalogPageDto;
import ru.yandex.practicum.mymarket.item.dto.ItemDto;
import ru.yandex.practicum.mymarket.item.dto.PagingDto;
import ru.yandex.practicum.mymarket.item.dto.SortType;
import ru.yandex.practicum.mymarket.item.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.support.AbstractWebMvcTest;
import ru.yandex.practicum.mymarket.support.TestItems;

class ItemControllerTest extends AbstractWebMvcTest {

    private static final ItemDto BALL = TestItems.itemDto(1, "Мяч", 100, 2);

    @Test
    void rootShowsCatalogWithDefaultParameters() throws Exception {
        PagingDto paging = new PagingDto(5, 1, false, false);
        List<List<ItemDto>> rows = List.of(List.of(BALL, ItemDto.stub(), ItemDto.stub()));
        when(itemService.getCatalogPage("", SortType.NO, 1, 5)).thenReturn(new CatalogPageDto(rows, paging));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("items", rows))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("sort", "NO"))
                .andExpect(model().attribute("paging", paging))
                .andExpect(content().string(containsString("Мяч")))
                .andExpect(content().string(containsString("src=\"/images/1.svg\"")));
    }

    @Test
    void itemsPassesRequestParametersToService() throws Exception {
        PagingDto paging = new PagingDto(10, 2, true, true);
        when(itemService.getCatalogPage(eq("мяч"), eq(SortType.PRICE), anyInt(), anyInt()))
                .thenReturn(new CatalogPageDto(List.of(), paging));

        mockMvc.perform(get("/items")
                        .param("search", "мяч")
                        .param("sort", "PRICE")
                        .param("pageNumber", "2")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sort", "PRICE"))
                .andExpect(content().string(containsString("Страница: 2")))
                .andExpect(content().string(containsString("value=\"PRICE\" selected")));

        verify(itemService).getCatalogPage("мяч", SortType.PRICE, 2, 10);
    }

    @Test
    void unknownSortIsBadRequest() throws Exception {
        mockMvc.perform(get("/items").param("sort", "UNKNOWN")).andExpect(status().isBadRequest());
    }

    @Test
    void changeCountFromCatalogRedirectsWithSameParameters() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "PLUS")
                        .param("search", "ball")
                        .param("sort", "ALPHA")
                        .param("pageNumber", "3")
                        .param("pageSize", "20"))
                .andExpect(redirectedUrl("/items?search=ball&sort=ALPHA&pageNumber=3&pageSize=20"));

        verify(cartService).changeItemCount(1, CartAction.PLUS);
    }

    @Test
    void changeCountFromCatalogRequiresAction() throws Exception {
        mockMvc.perform(post("/items").param("id", "1")).andExpect(status().isBadRequest());
    }

    @Test
    void itemPageShowsItem() throws Exception {
        when(itemService.getItem(1)).thenReturn(BALL);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", BALL));
    }

    @Test
    void missingItemIsNotFound() throws Exception {
        when(itemService.getItem(anyLong())).thenThrow(new ItemNotFoundException(404));

        mockMvc.perform(get("/items/404")).andExpect(status().isNotFound());
    }

    @Test
    void changeCountFromItemPageRendersItem() throws Exception {
        when(itemService.getItem(1)).thenReturn(BALL);

        mockMvc.perform(post("/items/1").param("action", "MINUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", BALL));

        verify(cartService).changeItemCount(1, CartAction.MINUS);
    }

    @Test
    void itemInCartHasDeleteButtonInsteadOfAddButton() throws Exception {
        when(itemService.getItem(1)).thenReturn(BALL);

        mockMvc.perform(get("/items/1"))
                .andExpect(content().string(containsString("value=\"DELETE\"")))
                .andExpect(content().string(not(containsString("btn-warning"))));
    }

    @Test
    void itemOutsideCartHasAddButtonInsteadOfDeleteButton() throws Exception {
        when(itemService.getItem(2)).thenReturn(TestItems.itemDto(2, "Палатка", 1000, 0));

        mockMvc.perform(get("/items/2"))
                .andExpect(content().string(containsString("btn-warning")))
                .andExpect(content().string(not(containsString("value=\"DELETE\""))));
    }

    @Test
    void catalogShowsDeleteButtonOnlyForItemsInCart() throws Exception {
        ItemDto outsideCart = TestItems.itemDto(2, "Палатка", 1000, 0);
        PagingDto paging = new PagingDto(5, 1, false, false);
        when(itemService.getCatalogPage("", SortType.NO, 1, 5)).thenReturn(
                new CatalogPageDto(List.of(List.of(BALL, outsideCart, ItemDto.stub())), paging));

        String page = mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(page).containsOnlyOnce("value=\"DELETE\"").containsOnlyOnce("btn-warning");
    }

    @Test
    void deleteFromCatalogRemovesItemAndRedirects() throws Exception {
        mockMvc.perform(post("/items").param("id", "1").param("action", "DELETE"))
                .andExpect(redirectedUrl("/items?search=&sort=NO&pageNumber=1&pageSize=5"));

        verify(cartService).changeItemCount(1, CartAction.DELETE);
    }
}
