package ru.yandex.practicum.mymarket.item.controller;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.item.exception.ItemImportException;
import ru.yandex.practicum.mymarket.item.service.ItemImportService;

@Controller
public class ItemImportController {
    private final ItemImportService imports;
    public ItemImportController(ItemImportService imports) { this.imports = imports; }
    @GetMapping("/items/import")
    public String page() { return "items-import"; }
    @PostMapping("/items/import")
    public Mono<String> upload(@RequestPart("file") FilePart file, Model model) {
        return DataBufferUtils.join(file.content()).flatMap(buffer -> {
            try {
                byte[] bytes = new byte[buffer.readableByteCount()];
                buffer.read(bytes);
                return imports.importItems(new java.io.ByteArrayInputStream(bytes));
            } finally { DataBufferUtils.release(buffer); }
        }).map(count -> { model.addAttribute("imported", count); return "items-import"; })
          .onErrorResume(ItemImportException.class, ex -> {
              model.addAttribute("error", ex.getMessage()); return Mono.just("items-import");
          });
    }
}
