package ru.yandex.practicum.mymarket.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.cart.service.CartService;
import ru.yandex.practicum.mymarket.item.service.ImageService;
import ru.yandex.practicum.mymarket.item.service.ItemImportService;
import ru.yandex.practicum.mymarket.item.service.ItemService;
import ru.yandex.practicum.mymarket.order.service.OrderService;
import ru.yandex.practicum.mymarket.purchase.service.PurchaseService;

/**
 * Единая конфигурация веб-слоя: все контроллеры и все моки объявлены здесь,
 * поэтому наследники используют один закешированный контекст.
 */
@WebMvcTest
public abstract class AbstractWebMvcTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected ItemService itemService;

    @MockitoBean
    protected ItemImportService itemImportService;

    @MockitoBean
    protected ImageService imageService;

    @MockitoBean
    protected CartService cartService;

    @MockitoBean
    protected OrderService orderService;

    @MockitoBean
    protected PurchaseService purchaseService;
}
