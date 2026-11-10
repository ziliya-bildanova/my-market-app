package ru.yandex.practicum.mymarket.purchase.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.mymarket.support.AbstractWebMvcTest;

class PurchaseControllerTest extends AbstractWebMvcTest {

    @Test
    void buyRedirectsToNewOrder() throws Exception {
        when(purchaseService.purchase()).thenReturn(Optional.of(7L));

        mockMvc.perform(post("/buy")).andExpect(redirectedUrl("/orders/7?newOrder=true"));
    }

    @Test
    void buyWithEmptyCartRedirectsToCart() throws Exception {
        when(purchaseService.purchase()).thenReturn(Optional.empty());

        mockMvc.perform(post("/buy")).andExpect(redirectedUrl("/cart/items"));
    }
}
