package ru.yandex.practicum.mymarket.cart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.dto.CartAction;
import ru.yandex.practicum.mymarket.cart.service.CartService;

@Controller
@RequestMapping("/cart/items")
public class CartController {
    private final CartService cart;
    public CartController(CartService cart) { this.cart = cart; }
    @GetMapping
    public Mono<String> getCart(Model model) {
        return cart.getCart().map(value -> {
            model.addAttribute("items", value.items()); model.addAttribute("total", value.total()); return "cart";
        });
    }
    @PostMapping
    public Mono<String> change(org.springframework.web.server.ServerWebExchange exchange, Model model) {
        return exchange.getFormData().flatMap(form -> cart.changeItemCount(
                Long.parseLong(form.getFirst("id")), CartAction.valueOf(form.getFirst("action"))))
                .then(getCart(model));
    }
}
