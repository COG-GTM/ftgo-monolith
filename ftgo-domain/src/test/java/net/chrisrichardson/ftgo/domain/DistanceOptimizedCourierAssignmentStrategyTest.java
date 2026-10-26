package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DistanceOptimizedCourierAssignmentStrategyTest {

  private DistanceOptimizedCourierAssignmentStrategy strategy;

  @Before
  public void setUp() {
    strategy = new DistanceOptimizedCourierAssignmentStrategy();
  }

  @Test
  public void shouldThrowWhenNoCouriersAvailable() {
    Order order = makeOrder(40.7128, -74.0060);
    assertThatThrownBy(() -> strategy.assignCourier(Collections.emptyList(), order))
            .isInstanceOf(NoCourierAvailableException.class);
  }

  @Test
  public void shouldAssignClosestCourier() {
    Order order = makeOrder(40.7128, -74.0060);

    Courier close = makeCourier(40.7130, -74.0062);
    close.noteAvailable();
    Courier far = makeCourier(40.8000, -73.9000);
    far.noteAvailable();

    Courier assigned = strategy.assignCourier(Arrays.asList(far, close), order);
    assertThat(assigned).isEqualTo(close);
  }

  @Test
  public void shouldFallbackToLoadBalanceWhenNoRestaurantLocation() {
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(new MenuItem("i1", "Item", new Money("10.00"))));
    Restaurant restaurant = new Restaurant("R", new Address("1", null, "C", "S", "Z"), menu);
    List<OrderLineItem> items = Arrays.asList(new OrderLineItem("i1", "Item", new Money("10.00"), 1));
    Order order = new Order(1L, restaurant, items);
    order.setId(1L);

    Courier c1 = makeCourier(40.0, -74.0);
    c1.noteAvailable();
    Courier c2 = makeCourier(41.0, -73.0);
    c2.noteAvailable();

    Courier assigned = strategy.assignCourier(Arrays.asList(c1, c2), order);
    assertThat(assigned).isNotNull();
  }

  @Test
  public void shouldSkipCouriersAtMaxCapacity() {
    Order order = makeOrder(40.7128, -74.0060);

    Courier overloaded = makeCourier(40.7129, -74.0061);
    overloaded.noteAvailable();
    // Add 5 pickups to max out
    for (int i = 0; i < 5; i++) {
      Order o = makeOrder(40.0 + i * 0.01, -74.0);
      o.setId((long)(i + 10));
      overloaded.addAction(Action.makePickup(o));
    }

    Courier available = makeCourier(40.8000, -73.9000);
    available.noteAvailable();

    Courier assigned = strategy.assignCourier(Arrays.asList(overloaded, available), order);
    assertThat(assigned).isEqualTo(available);
  }

  @Test
  public void shouldAssignByLoadBalanceWhenAllMaxed() {
    Order order = makeOrder(40.7128, -74.0060);

    Courier c1 = makeCourier(40.7129, -74.0061);
    c1.noteAvailable();
    for (int i = 0; i < 5; i++) {
      Order o = makeOrder(40.0 + i * 0.01, -74.0);
      o.setId((long)(i + 10));
      c1.addAction(Action.makePickup(o));
    }

    Courier c2 = makeCourier(40.7200, -74.0100);
    c2.noteAvailable();
    for (int i = 0; i < 5; i++) {
      Order o = makeOrder(40.0 + i * 0.01, -74.0);
      o.setId((long)(i + 20));
      c2.addAction(Action.makePickup(o));
    }

    // Both maxed out - should fall back to least-loaded (both same load, picks first)
    Courier assigned = strategy.assignCourier(Arrays.asList(c1, c2), order);
    assertThat(assigned).isNotNull();
  }

  @Test
  public void shouldHandleCourierWithoutLocation() {
    Order order = makeOrder(40.7128, -74.0060);

    Courier noLocation = new Courier(new PersonName("X", "Y"), new Address("1", null, "C", "S", "Z"));
    noLocation.noteAvailable();

    Courier withLocation = makeCourier(40.7130, -74.0062);
    withLocation.noteAvailable();

    Courier assigned = strategy.assignCourier(Arrays.asList(noLocation, withLocation), order);
    assertThat(assigned).isNotNull();
  }

  @Test
  public void haversineDistanceShouldBeZeroForSamePoint() {
    double distance = DistanceOptimizedCourierAssignmentStrategy.haversineDistance(40.0, -74.0, 40.0, -74.0);
    assertThat(distance).isEqualTo(0.0);
  }

  @Test
  public void haversineDistanceShouldBePositiveForDifferentPoints() {
    double distance = DistanceOptimizedCourierAssignmentStrategy.haversineDistance(40.7128, -74.0060, 34.0522, -118.2437);
    assertThat(distance).isGreaterThan(0);
  }

  @Test
  public void estimateDeliveryMinutesShouldIncludeBaseTime() {
    double minutes = DistanceOptimizedCourierAssignmentStrategy.estimateDeliveryMinutes(0);
    assertThat(minutes).isEqualTo(5.0);
  }

  @Test
  public void estimateDeliveryMinutesShouldScaleWithDistance() {
    double minutes = DistanceOptimizedCourierAssignmentStrategy.estimateDeliveryMinutes(10.0);
    assertThat(minutes).isGreaterThan(5.0);
  }

  private Order makeOrder(double lat, double lng) {
    Address addr = new Address("1", null, "C", "S", "Z", lat, lng);
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(new MenuItem("i1", "Item", new Money("10.00"))));
    Restaurant restaurant = new Restaurant("R", addr, menu);
    List<OrderLineItem> items = Arrays.asList(new OrderLineItem("i1", "Item", new Money("10.00"), 1));
    Order order = new Order(1L, restaurant, items);
    order.setId(1L);
    return order;
  }

  private Courier makeCourier(double lat, double lng) {
    return new Courier(new PersonName("C", "D"), new Address("1", null, "C", "S", "Z", lat, lng));
  }
}
