package ru.yandex.practicum.mymarket.item.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.mymarket.cart.dto.CartAction;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.item.dto.CatalogPageDto;
import ru.yandex.practicum.mymarket.item.dto.SortType;
import ru.yandex.practicum.mymarket.item.service.ItemService;

@Controller
public class ItemController {

    private static final String DEFAULT_SORT = "NO";
    private static final String DEFAULT_PAGE_NUMBER = "1";
    private static final String DEFAULT_PAGE_SIZE = "5";

    private final ItemService itemService;
    private final CartService cartService;

    public ItemController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping({"/", "/items"})
    public String getItems(@RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = DEFAULT_SORT) SortType sort,
                           @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber,
                           @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
                           Model model) {
        CatalogPageDto page = itemService.getCatalogPage(search, sort, pageNumber, pageSize);
        model.addAttribute("items", page.rows());
        model.addAttribute("search", search);
        model.addAttribute("sort", sort.name());
        model.addAttribute("paging", page.paging());
        return "items";
    }

    @PostMapping("/items")
    public String changeCountFromCatalog(@RequestParam long id,
                                         @RequestParam CartAction action,
                                         @RequestParam(defaultValue = "") String search,
                                         @RequestParam(defaultValue = DEFAULT_SORT) SortType sort,
                                         @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int pageNumber,
                                         @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
                                         RedirectAttributes redirectAttributes) {
        cartService.changeItemCount(id, action);
        redirectAttributes.addAttribute("search", search);
        redirectAttributes.addAttribute("sort", sort.name());
        redirectAttributes.addAttribute("pageNumber", pageNumber);
        redirectAttributes.addAttribute("pageSize", pageSize);
        return "redirect:/items";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable long id, Model model) {
        model.addAttribute("item", itemService.getItem(id));
        return "item";
    }

    @PostMapping("/items/{id}")
    public String changeCountFromItemPage(@PathVariable long id, @RequestParam CartAction action, Model model) {
        cartService.changeItemCount(id, action);
        model.addAttribute("item", itemService.getItem(id));
        return "item";
    }
}
