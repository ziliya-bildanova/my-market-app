package ru.yandex.practicum.mymarket;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.cart.dto.CartAction;
import ru.yandex.practicum.mymarket.cart.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.item.model.Item;
import ru.yandex.practicum.mymarket.item.repository.ItemRepository;
import ru.yandex.practicum.mymarket.order.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.order.repository.OrderRepository;
import ru.yandex.practicum.mymarket.order.service.OrderService;
import ru.yandex.practicum.mymarket.purchase.service.PurchaseService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class MarketReactiveTest {
    @Autowired WebTestClient web;
    @Autowired ItemRepository items;
    @Autowired CartItemRepository cartItems;
    @Autowired OrderRepository orders;
    @Autowired OrderItemRepository lines;
    @Autowired CartService cart;
    @Autowired PurchaseService purchases;
    @Autowired OrderService orderService;

    @BeforeEach
    void clean() {
        lines.deleteAll().then(orders.deleteAll()).then(cartItems.deleteAll()).then(items.deleteAll()).block();
    }

    @Test
    void catalogSearchSortingAndPagination() {
        items.save(new Item("Яблоко", "Фрукт", "images/ball.svg", 30)).block();
        items.save(new Item("Арбуз", "Ягода", "images/ball.svg", 20)).block();
        items.save(new Item("Банан", "Фрукт", "images/ball.svg", 10)).block();
        web.get().uri("/items?search=Фрукт&sort=PRICE&pageSize=2").exchange().expectStatus().isOk()
                .expectBody(String.class).value(body -> {
                    assertThat(body).contains("Банан", "Яблоко").doesNotContain("Арбуз");
                    assertThat(body.indexOf("Банан")).isLessThan(body.indexOf("Яблоко"));
                });
        web.get().uri("/items?sort=ALPHA&pageSize=2").exchange().expectStatus().isOk()
                .expectBody(String.class).value(body -> assertThat(body).contains("Арбуз", "Банан").doesNotContain("Яблоко"));
    }

    @Test
    void cartPurchaseAndOrderSnapshot() {
        Item ball = items.save(new Item("Мяч", "Футбольный", "images/ball.svg", 100)).block();
        Item tent = items.save(new Item("Палатка", "Походная", "images/tent.svg", 1000)).block();
        web.post().uri("/items").body(org.springframework.web.reactive.function.BodyInserters
                .fromFormData("id", ball.getId().toString()).with("action", "PLUS"))
                .exchange().expectStatus().is3xxRedirection();
        StepVerifier.create(cart.changeItemCount(ball.getId(), CartAction.PLUS)).verifyComplete();
        StepVerifier.create(cart.changeItemCount(tent.getId(), CartAction.PLUS)).verifyComplete();
        StepVerifier.create(cart.getCart()).assertNext(value -> assertThat(value.total()).isEqualTo(1200)).verifyComplete();
        web.get().uri("/cart/items").exchange().expectStatus().isOk().expectBody(String.class)
                .value(body -> assertThat(body).contains("Итого: 1200 руб."));
        Long id = purchases.purchase().block();
        assertThat(id).isNotNull();
        StepVerifier.create(cartItems.count()).expectNext(0L).verifyComplete();
        StepVerifier.create(orderService.getOrder(id)).assertNext(value -> {
            assertThat(value.totalSum()).isEqualTo(1200);
            assertThat(value.items()).hasSize(2);
        }).verifyComplete();
        web.get().uri("/orders/" + id).exchange().expectStatus().isOk().expectBody(String.class)
                .value(body -> assertThat(body).contains("Мяч", "Палатка", "Сумма: 1200 руб."));
        web.get().uri("/orders").exchange().expectStatus().isOk().expectBody(String.class)
                .value(body -> assertThat(body).contains("Мяч (2 шт.) 200 руб."));
        StepVerifier.create(purchases.purchase()).verifyComplete();
    }

    @Test
    void importAndImagesAndMissingResources() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", "title;description;imgPath;price\nГантели;Набор;;4900\n".getBytes(StandardCharsets.UTF_8))
                .filename("items.csv").contentType(org.springframework.http.MediaType.TEXT_PLAIN);
        web.post().uri("/items/import").bodyValue(builder.build()).exchange().expectStatus().isOk()
                .expectBody(String.class).value(body -> assertThat(body).contains("Загружено товаров: 1"));
        web.get().uri("/items").exchange().expectStatus().isOk().expectBody(String.class)
                .value(body -> assertThat(body).contains("Гантели", "/images/no-image.svg"));
        web.get().uri("/images/ball.svg").exchange().expectStatus().isOk().expectHeader().contentType("image/svg+xml");
        web.get().uri("/items/999999").exchange().expectStatus().isNotFound();
        web.get().uri("/orders/999999").exchange().expectStatus().isNotFound();
    }

    @Test
    void quantitiesAndPurchaseViaWeb() {
        Item item = items.save(new Item("Тестовый товар", "Описание", "images/ball.svg", 75)).block();
        web.post().uri("/items/" + item.getId()).body(org.springframework.web.reactive.function.BodyInserters
                .fromFormData("action", "PLUS")).exchange().expectStatus().isOk()
                .expectBody(String.class).value(body -> assertThat(body).contains("Тестовый товар"));
        web.post().uri("/cart/items").body(org.springframework.web.reactive.function.BodyInserters
                .fromFormData("id", item.getId().toString()).with("action", "PLUS"))
                .exchange().expectStatus().isOk().expectBody(String.class)
                .value(body -> assertThat(body).contains("Итого: 150 руб."));
        web.post().uri("/buy").exchange().expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/orders/\\d+\\?newOrder=true");
        StepVerifier.create(cartItems.count()).expectNext(0L).verifyComplete();
        web.post().uri("/buy").exchange().expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");
        StepVerifier.create(orders.count()).expectNext(1L).verifyComplete();
    }

    @Test
    void deleteAndInvalidImport() {
        Item item = items.save(new Item("Зонт", "Новый", "images/umbrella.svg", 250)).block();
        StepVerifier.create(cart.changeItemCount(item.getId(), CartAction.PLUS)).verifyComplete();
        StepVerifier.create(cart.changeItemCount(item.getId(), CartAction.MINUS)).verifyComplete();
        StepVerifier.create(cartItems.count()).expectNext(0L).verifyComplete();
        StepVerifier.create(cart.changeItemCount(item.getId(), CartAction.PLUS)).verifyComplete();
        StepVerifier.create(cart.changeItemCount(item.getId(), CartAction.DELETE)).verifyComplete();
        StepVerifier.create(cartItems.count()).expectNext(0L).verifyComplete();
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", "title;description;imgPath;price\nНовый;Описание;;99\nПлохой;Товар;;-1\n"
                .getBytes(StandardCharsets.UTF_8)).filename("invalid.csv");
        web.post().uri("/items/import").bodyValue(builder.build()).exchange().expectStatus().isOk()
                .expectBody(String.class).value(body -> assertThat(body).contains("цена не может быть отрицательной"));
        StepVerifier.create(items.count()).expectNext(1L).verifyComplete();
    }
}
