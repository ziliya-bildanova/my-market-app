package ru.yandex.practicum.mymarket.item.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.item.service.ImageService;

@Controller
public class ImageController {
    private final ImageService images;
    public ImageController(ImageService images) { this.images = images; }
    @GetMapping("/images/{name}")
    public Mono<ResponseEntity<Resource>> getImage(@PathVariable String name) {
        return Mono.fromSupplier(() -> ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(name).orElse(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM))
                .body(images.getImage(name)));
    }
}
