package ru.yandex.practicum.mymarket.item.service;

import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mymarket.item.exception.ImageNotFoundException;

@Service
public class ImageService {

    private static final String IMAGES_LOCATION = "images/";
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9_-]+\\.[A-Za-z0-9]+");

    public Resource getImage(String name) {
        if (!SAFE_NAME.matcher(name).matches()) {
            throw new ImageNotFoundException(name);
        }
        Resource image = new ClassPathResource(IMAGES_LOCATION + name);
        if (!image.exists()) {
            throw new ImageNotFoundException(name);
        }
        return image;
    }
}
