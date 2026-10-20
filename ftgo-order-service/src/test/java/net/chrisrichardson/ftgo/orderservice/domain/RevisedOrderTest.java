package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.LineItemQuantityChange;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.RestaurantMother;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class RevisedOrderTest {

  @Test
  public void exposesTheOrderAndQuantityChangeItWasCreatedWith() {
    Order order = new Order(OrderDetailsMother.CONSUMER_ID, RestaurantMother.AJANTA_RESTAURANT,
            OrderDetailsMother.chickenVindalooLineItems());
    LineItemQuantityChange change = new LineItemQuantityChange(new Money("61.70"), new Money("74.04"), new Money("12.34"));

    RevisedOrder revisedOrder = new RevisedOrder(order, change);

    assertSame(order, revisedOrder.getOrder());
    assertSame(change, revisedOrder.getChange());
    assertEquals(new Money("12.34"), revisedOrder.getChange().getDelta());
  }

  @Test
  public void allowsNullComponents() {
    RevisedOrder revisedOrder = new RevisedOrder(null, null);

    assertNull(revisedOrder.getOrder());
    assertNull(revisedOrder.getChange());
  }
}
