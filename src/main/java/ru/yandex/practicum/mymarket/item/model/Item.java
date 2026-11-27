package ru.yandex.practicum.mymarket.item.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("items")
public class Item {
    @Id @Column("id") private Long id;
    @Column("title") private String title;
    @Column("description") private String description;
    @Column("img_path") private String imgPath;
    @Column("price") private long price;

    public Item() {}
    public Item(String title, String description, String imgPath, long price) {
        this.title = title; this.description = description; this.imgPath = imgPath; this.price = price;
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImgPath() { return imgPath; }
    public long getPrice() { return price; }
}
