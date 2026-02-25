package com.ftgo.domain;

import com.ftgo.common.Money;
import com.ftgo.common.UnsupportedStateTransitionException;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class OrderTest {

    private Restaurant restaurant;
    private Order order;

    @Before
    public void setUp() {
        restaurant = new Restaurant(1L, "Test Restaurant",
                new RestaurantMenu(Arrays.asList(
                        new MenuItem("item1", "Burger", new Money(10)),
                        new MenuItem("item2", "Fries", new Money(5))
                )));

        order = new Order(1L, restaurant, Arrays.asList(
                new OrderLineItem("item1", "Burger", new Money(10), 2),
                new OrderLineItem("item2", "Fries", new Money(5), 1)
        ));
    }

    @Test
    public void shouldCreateOrderInApprovedState() {
        assertEquals(OrderState.APPROVED, order.getOrderState());
    }

    @Test
    public void shouldCalculateOrderTotal() {
        // 2 * $10 + 1 * $5 = $25
        assertEquals(new Money(25), order.getOrderTotal());
    }

    @Test
    public void shouldCancelApprovedOrder() {
        order.cancel();
        assertEquals(OrderState.CANCELLED, order.getOrderState());
    }

    @Test(expected = UnsupportedStateTransitionException.class)
    public void shouldNotCancelNonApprovedOrder() {
        order.cancel();
        order.cancel(); // Already cancelled, should throw
    }

    @Test
    public void shouldAcceptTicket() {
        LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
        order.acceptTicket(readyBy);
        assertEquals(OrderState.ACCEPTED, order.getOrderState());
    }

    @Test(expected = UnsupportedStateTransitionException.class)
    public void shouldNotAcceptTicketForCancelledOrder() {
        order.cancel();
        order.acceptTicket(LocalDateTime.now().plusHours(1));
    }

    @Test
    public void shouldTransitionThroughStates() {
        LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
        order.acceptTicket(readyBy);
        assertEquals(OrderState.ACCEPTED, order.getOrderState());

        order.notePreparing();
        assertEquals(OrderState.PREPARING, order.getOrderState());

        order.noteReadyForPickup();
        assertEquals(OrderState.READY_FOR_PICKUP, order.getOrderState());

        order.notePickedUp();
        assertEquals(OrderState.PICKED_UP, order.getOrderState());

        order.noteDelivered();
        assertEquals(OrderState.DELIVERED, order.getOrderState());
    }

    @Test
    public void shouldReturnConsumerId() {
        assertEquals(Long.valueOf(1L), order.getConsumerId());
    }

    @Test
    public void shouldReturnRestaurant() {
        assertSame(restaurant, order.getRestaurant());
    }

    @Test
    public void shouldReturnLineItems() {
        assertEquals(2, order.getLineItems().size());
    }
}
