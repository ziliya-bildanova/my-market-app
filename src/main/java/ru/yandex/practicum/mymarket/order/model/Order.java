package ru.yandex.practicum.mymarket.order.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("orders")
public class Order {
    @Id @Column("id") private Long id;
    @Column("created_at") private LocalDateTime createdAt;
    public Order() {}
    public Order(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
