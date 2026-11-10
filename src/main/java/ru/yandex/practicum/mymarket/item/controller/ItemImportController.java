package ru.yandex.practicum.mymarket.item.controller;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.mymarket.item.exception.ItemImportException;
import ru.yandex.practicum.mymarket.item.service.ItemImportService;

@Controller
public class ItemImportController {

    private final ItemImportService itemImportService;

    public ItemImportController(ItemImportService itemImportService) {
        this.itemImportService = itemImportService;
    }

    @GetMapping("/items/import")
    public String getImportPage() {
        return "items-import";
    }

    @PostMapping("/items/import")
    public String importItems(@RequestParam MultipartFile file, RedirectAttributes redirectAttributes) {
        try (InputStream csv = file.getInputStream()) {
            int imported = itemImportService.importItems(csv);
            redirectAttributes.addFlashAttribute("imported", imported);
        } catch (ItemImportException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось прочитать файл");
        }
        return "redirect:/items/import";
    }
}
