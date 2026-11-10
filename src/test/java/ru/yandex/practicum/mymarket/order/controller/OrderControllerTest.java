package ru.yandex.practicum.mymarket.order.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.mymarket.item.dto.ItemDto;
import ru.yandex.practicum.mymarket.order.dto.OrderDto;
import ru.yandex.practicum.mymarket.order.exception.OrderNotFoundException;
import ru.yandex.practicum.mymarket.support.AbstractWebMvcTest;

class OrderControllerTest extends AbstractWebMvcTest {

    private static final OrderDto ORDER =
            new OrderDto(7, List.of(new ItemDto(1, "Мяч", null, null, 100, 2)), 200);

    @Test
    void ordersAreListed() throws Exception {
        when(orderService.getOrders()).thenReturn(List.of(ORDER));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", List.of(ORDER)))
                .andExpect(content().string(containsString("Заказ №7")));
    }

    @Test
    void orderIsNotNewByDefault() throws Exception {
        when(orderService.getOrder(7)).thenReturn(ORDER);

        mockMvc.perform(get("/orders/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", ORDER))
                .andExpect(model().attribute("newOrder", false))
                .andExpect(content().string(not(containsString("Поздравляем"))));
    }

    @Test
    void newOrderShowsCongratulation() throws Exception {
        when(orderService.getOrder(7)).thenReturn(ORDER);

        mockMvc.perform(get("/orders/7").param("newOrder", "true"))
                .andExpect(model().attribute("newOrder", true))
                .andExpect(content().string(containsString("Поздравляем")));
    }

    @Test
    void missingOrderIsNotFound() throws Exception {
        when(orderService.getOrder(404)).thenThrow(new OrderNotFoundException(404));

        mockMvc.perform(get("/orders/404")).andExpect(status().isNotFound());
    }
}
