package ru.yandex.practicum.mymarket.cart.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.mymarket.cart.dto.CartAction;
import ru.yandex.practicum.mymarket.cart.dto.CartDto;
import ru.yandex.practicum.mymarket.item.dto.ItemDto;
import ru.yandex.practicum.mymarket.support.AbstractWebMvcTest;
import ru.yandex.practicum.mymarket.support.TestItems;

class CartControllerTest extends AbstractWebMvcTest {

    private static final List<ItemDto> ITEMS = List.of(TestItems.itemDto(1, "Мяч", 100, 2));

    @Test
    void cartIsShownWithTotal() throws Exception {
        when(cartService.getCart()).thenReturn(new CartDto(ITEMS, 200));

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", ITEMS))
                .andExpect(model().attribute("total", 200L))
                .andExpect(content().string(containsString("Итого: 200 руб.")));
    }

    @Test
    void emptyCartHasNoBuyButton() throws Exception {
        when(cartService.getCart()).thenReturn(new CartDto(List.of(), 0));

        mockMvc.perform(get("/cart/items"))
                .andExpect(content().string(not(containsString("Купить"))));
    }

    @Test
    void changeCountRendersCart() throws Exception {
        when(cartService.getCart()).thenReturn(new CartDto(List.of(), 0));

        mockMvc.perform(post("/cart/items").param("id", "1").param("action", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        verify(cartService).changeItemCount(1, CartAction.DELETE);
    }
}
