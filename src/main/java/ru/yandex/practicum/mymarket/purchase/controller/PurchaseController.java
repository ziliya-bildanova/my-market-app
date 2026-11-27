package ru.yandex.practicum.mymarket.purchase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.purchase.service.PurchaseService;

@Controller
public class PurchaseController {
    private final PurchaseService purchases;
    public PurchaseController(PurchaseService purchases) { this.purchases = purchases; }
    @PostMapping("/buy")
    public Mono<String> buy() {
        return purchases.purchase().map(id -> "redirect:/orders/" + id + "?newOrder=true")
                .defaultIfEmpty("redirect:/cart/items");
    }
}
