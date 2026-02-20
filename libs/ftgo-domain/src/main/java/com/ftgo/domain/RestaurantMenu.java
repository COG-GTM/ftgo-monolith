package com.ftgo.domain;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.util.List;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class RestaurantMenu {

    @Embedded
    @ElementCollection
    @CollectionTable(name = "restaurant_menu_items")
    private List<MenuItem> menuItems;

    private RestaurantMenu() {
    }

    public RestaurantMenu(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestaurantMenu that = (RestaurantMenu) o;
        return Objects.equals(menuItems, that.menuItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuItems);
    }

    @Override
    public String toString() {
        return "RestaurantMenu{menuItems=" + menuItems + '}';
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }
}
