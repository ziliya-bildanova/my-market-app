package ru.yandex.practicum.mymarket.order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.order.service.OrderService;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orders;
    public OrderController(OrderService orders) { this.orders = orders; }
    @GetMapping
    public Mono<String> getOrders(Model model) {
        return orders.getOrders().collectList().map(found -> { model.addAttribute("orders", found); return "orders"; });
    }
    @GetMapping("/{id}")
    public Mono<String> getOrder(@PathVariable long id, @RequestParam(defaultValue = "false") boolean newOrder, Model model) {
        return orders.getOrder(id).map(order -> {
            model.addAttribute("order", order); model.addAttribute("newOrder", newOrder); return "order";
        });
    }
}
