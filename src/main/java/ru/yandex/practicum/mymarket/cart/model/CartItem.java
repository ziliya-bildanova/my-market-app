package ru.yandex.practicum.mymarket.cart.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import ru.yandex.practicum.mymarket.item.model.Item;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, unique = true)
    private Item item;

    @Column(nullable = false)
    private int quantity;

    protected CartItem() {
    }

    public CartItem(Item item) {
        this.item = item;
        this.quantity = 1;
    }

    public void increaseQuantity() {
        quantity++;
    }

    public void decreaseQuantity() {
        quantity--;
    }

    public boolean isLastUnit() {
        return quantity == 1;
    }

    public Long getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }
}
