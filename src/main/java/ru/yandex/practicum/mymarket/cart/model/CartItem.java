package ru.yandex.practicum.mymarket.cart.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cart_items")
public class CartItem {
    @Id @Column("id") private Long id;
    @Column("item_id") private Long itemId;
    @Column("quantity") private int quantity;

    public CartItem() {}
    public CartItem(long itemId, int quantity) { this.itemId = itemId; this.quantity = quantity; }
    public Long getId() { return id; }
    public Long getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
