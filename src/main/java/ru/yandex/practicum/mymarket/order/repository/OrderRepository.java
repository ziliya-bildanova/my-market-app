package ru.yandex.practicum.mymarket.order.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.mymarket.order.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "items")
    Optional<Order> findWithItemsById(Long id);
}
