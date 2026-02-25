package com.ftgo.domain;

import com.ftgo.common.Money;
import org.junit.Test;

import static org.junit.Assert.*;

public class MoneyValueObjectTest {

    @Test
    public void shouldCreateOrderLineItemWithMoney() {
        Money price = new Money(10);
        OrderLineItem item = new OrderLineItem("item1", "Burger", price, 2);

        assertEquals(new Money(20), item.getTotal());
        assertEquals("item1", item.getMenuItemId());
        assertEquals("Burger", item.getName());
        assertEquals(new Money(10), item.getPrice());
        assertEquals(2, item.getQuantity());
    }

    @Test
    public void shouldCalculateDeltaForChangedQuantity() {
        OrderLineItem item = new OrderLineItem("item1", "Burger", new Money(10), 2);
        // Changing from 2 to 5 = 3 * $10 = $30 delta
        assertEquals(new Money(30), item.deltaForChangedQuantity(5));
    }

    @Test
    public void shouldCreateMenuItemWithMoney() {
        Money price = new Money("12.50");
        MenuItem item = new MenuItem("m1", "Pizza", price);

        assertEquals("m1", item.getId());
        assertEquals("Pizza", item.getName());
        assertEquals(price, item.getPrice());
    }

    @Test
    public void shouldCreateLineItemQuantityChange() {
        Money current = new Money(100);
        Money newTotal = new Money(130);
        Money delta = new Money(30);

        LineItemQuantityChange change = new LineItemQuantityChange(current, newTotal, delta);

        assertEquals(current, change.getCurrentOrderTotal());
        assertEquals(newTotal, change.getNewOrderTotal());
        assertEquals(delta, change.getDelta());
    }
}
