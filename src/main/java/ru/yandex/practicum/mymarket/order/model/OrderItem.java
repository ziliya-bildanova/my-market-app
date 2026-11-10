package ru.yandex.practicum.mymarket.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import ru.yandex.practicum.mymarket.item.model.Item;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private long price;

    @Column(nullable = false)
    private int quantity;

    protected OrderItem() {
    }

    public OrderItem(Item item, int quantity) {
        this.item = item;
        this.title = item.getTitle();
        this.price = item.getPrice();
        this.quantity = quantity;
    }

    void assignTo(Order order) {
        this.order = order;
    }

    public long getCost() {
        return price * quantity;
    }

    public Long getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public String getTitle() {
        return title;
    }

    public long getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}
