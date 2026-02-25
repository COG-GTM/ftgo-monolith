package com.ftgo.domain;

import com.ftgo.common.Address;
import com.ftgo.common.Money;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;

public class RestaurantTest {

    @Test
    public void shouldCreateRestaurant() {
        Address address = new Address("123 Main St", "", "Springfield", "IL", "62701");
        RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
                new MenuItem("item1", "Burger", new Money(10)),
                new MenuItem("item2", "Fries", new Money(5))
        ));

        Restaurant restaurant = new Restaurant("Test Restaurant", address, menu);

        assertEquals("Test Restaurant", restaurant.getName());
        assertEquals(address, restaurant.getAddress());
    }

    @Test
    public void shouldFindMenuItem() {
        RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
                new MenuItem("item1", "Burger", new Money(10)),
                new MenuItem("item2", "Fries", new Money(5))
        ));

        Restaurant restaurant = new Restaurant(1L, "Test Restaurant", menu);

        Optional<MenuItem> found = restaurant.findMenuItem("item1");
        assertTrue(found.isPresent());
        assertEquals("Burger", found.get().getName());
    }

    @Test
    public void shouldReturnEmptyForMissingMenuItem() {
        RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
                new MenuItem("item1", "Burger", new Money(10))
        ));

        Restaurant restaurant = new Restaurant(1L, "Test Restaurant", menu);

        Optional<MenuItem> found = restaurant.findMenuItem("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowOnReviseMenu() {
        Restaurant restaurant = new Restaurant();
        restaurant.reviseMenu(new RestaurantMenu(Arrays.asList()));
    }
}
