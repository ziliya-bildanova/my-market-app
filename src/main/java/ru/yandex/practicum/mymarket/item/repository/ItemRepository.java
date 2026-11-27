package ru.yandex.practicum.mymarket.item.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.mymarket.item.model.Item;

public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {
    @Query("SELECT * FROM items WHERE LOWER(title) LIKE :pattern ESCAPE '!' OR LOWER(description) LIKE :pattern ESCAPE '!' ORDER BY id")
    Flux<Item> search(String pattern);
}
