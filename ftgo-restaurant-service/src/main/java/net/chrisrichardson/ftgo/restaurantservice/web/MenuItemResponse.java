package net.chrisrichardson.ftgo.restaurantservice.web;

import net.chrisrichardson.ftgo.common.Money;

public class MenuItemResponse {
    private String id;
    private String name;
    private Money price;
    private String category;
    private boolean available;
    private String description;

    public MenuItemResponse() {
    }

    public MenuItemResponse(String id, String name, Money price, String category, boolean available, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.available = available;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Money getPrice() {
        return price;
    }

    public void setPrice(Money price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
