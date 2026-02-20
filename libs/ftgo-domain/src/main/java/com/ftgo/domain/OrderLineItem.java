package com.ftgo.domain;

import com.ftgo.common.Money;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.util.Objects;

@Embeddable
public class OrderLineItem {

    private int quantity;
    private String menuItemId;
    private String name;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "amount", column = @Column(name = "price")))
    private Money price;

    public OrderLineItem() {
    }

    public OrderLineItem(String menuItemId, String name, Money price, int quantity) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderLineItem that = (OrderLineItem) o;
        return quantity == that.quantity
                && Objects.equals(menuItemId, that.menuItemId)
                && Objects.equals(name, that.name)
                && Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, menuItemId, name, price);
    }

    @Override
    public String toString() {
        return "OrderLineItem{menuItemId='" + menuItemId + "', name='" + name + "', price=" + price + ", quantity=" + quantity + '}';
    }

    public Money deltaForChangedQuantity(int newQuantity) {
        return price.multiply(newQuantity - quantity);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
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

    public Money getTotal() {
        return price.multiply(quantity);
    }
}
