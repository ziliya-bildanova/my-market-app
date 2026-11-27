package ru.yandex.practicum.mymarket.order.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("order_items")
public class OrderItem {
    @Id @Column("id") private Long id;
    @Column("order_id") private Long orderId;
    @Column("item_id") private Long itemId;
    @Column("title") private String title;
    @Column("price") private long price;
    @Column("quantity") private int quantity;
    public OrderItem() {}
    public OrderItem(long orderId, long itemId, String title, long price, int quantity) {
        this.orderId = orderId; this.itemId = itemId; this.title = title; this.price = price; this.quantity = quantity;
    }
    public Long getId() { return id; }
    public Long getItemId() { return itemId; }
    public String getTitle() { return title; }
    public long getPrice() { return price; }
    public int getQuantity() { return quantity; }
}
