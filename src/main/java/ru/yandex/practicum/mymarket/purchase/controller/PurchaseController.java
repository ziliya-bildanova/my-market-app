package ru.yandex.practicum.mymarket.purchase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import ru.yandex.practicum.mymarket.purchase.service.PurchaseService;

@Controller
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping("/buy")
    public String buy() {
        return purchaseService.purchase()
                .map(orderId -> "redirect:/orders/" + orderId + "?newOrder=true")
                .orElse("redirect:/cart/items");
    }
}
