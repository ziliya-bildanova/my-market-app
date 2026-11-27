package ru.yandex.practicum.mymarket.item.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cart.dto.CartAction;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.item.dto.SortType;
import ru.yandex.practicum.mymarket.item.service.ItemService;

@Controller
public class ItemController {
    private final ItemService items;
    private final CartService cart;
    public ItemController(ItemService items, CartService cart) { this.items = items; this.cart = cart; }

    @GetMapping({"/", "/items"})
    public Mono<String> getItems(@RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize, Model model) {
        return items.getCatalogPage(search, sort, pageNumber, pageSize).map(page -> {
            model.addAttribute("items", page.rows());
            model.addAttribute("search", search);
            model.addAttribute("sort", sort.name());
            model.addAttribute("paging", page.paging());
            return "items";
        });
    }

    @PostMapping("/items")
    public Mono<String> changeFromCatalog(org.springframework.web.server.ServerWebExchange exchange) {
        return exchange.getFormData().flatMap(form -> {
            long id = Long.parseLong(form.getFirst("id"));
            CartAction action = CartAction.valueOf(form.getFirst("action"));
            String search = form.getFirst("search") == null ? "" : form.getFirst("search");
            String sort = form.getFirst("sort") == null ? "NO" : form.getFirst("sort");
            String number = form.getFirst("pageNumber") == null ? "1" : form.getFirst("pageNumber");
            String size = form.getFirst("pageSize") == null ? "5" : form.getFirst("pageSize");
            return cart.changeItemCount(id, action).thenReturn("redirect:" + UriComponentsBuilder.fromPath("/items")
                    .queryParam("search", search).queryParam("sort", sort).queryParam("pageNumber", number)
                    .queryParam("pageSize", size).build().encode().toUriString());
        });
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable long id, Model model) {
        return items.getItem(id).map(item -> { model.addAttribute("item", item); return "item"; });
    }

    @PostMapping("/items/{id}")
    public Mono<String> changeFromItem(@PathVariable long id, org.springframework.web.server.ServerWebExchange exchange, Model model) {
        return exchange.getFormData().flatMap(form -> cart.changeItemCount(id, CartAction.valueOf(form.getFirst("action"))))
                .then(getItem(id, model));
    }
}
