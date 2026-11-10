package ru.yandex.practicum.mymarket.item.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import ru.yandex.practicum.mymarket.item.exception.ImageNotFoundException;
import ru.yandex.practicum.mymarket.support.AbstractWebMvcTest;

class ImageControllerTest extends AbstractWebMvcTest {

    @Test
    void imageIsReturnedWithContentType() throws Exception {
        when(imageService.getImage("ball.png")).thenReturn(new ByteArrayResource(new byte[] {1, 2}));

        mockMvc.perform(get("/images/ball.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(new byte[] {1, 2}));
    }

    @Test
    void missingImageIsNotFound() throws Exception {
        when(imageService.getImage("missing.png")).thenThrow(new ImageNotFoundException("missing.png"));

        mockMvc.perform(get("/images/missing.png")).andExpect(status().isNotFound());
    }
}
