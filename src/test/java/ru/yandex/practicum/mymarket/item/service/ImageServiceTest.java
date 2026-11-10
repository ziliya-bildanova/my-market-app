package ru.yandex.practicum.mymarket.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.mymarket.item.exception.ImageNotFoundException;

class ImageServiceTest {

    private final ImageService imageService = new ImageService();

    @Test
    void existingImageIsFound() {
        assertThat(imageService.getImage("ball.svg").exists()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing.svg", "..", "../application.properties", "..\\secret.txt", "a/b.svg"})
    void missingOrUnsafeNamesAreRejected(String name) {
        assertThatThrownBy(() -> imageService.getImage(name)).isInstanceOf(ImageNotFoundException.class);
    }
}
