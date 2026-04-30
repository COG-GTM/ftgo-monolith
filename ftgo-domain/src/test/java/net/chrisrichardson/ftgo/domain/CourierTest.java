package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CourierTest {

  @Test
  public void shouldStartUnavailable() {
    Courier courier = new Courier();
    assertFalse(courier.isAvailable());
  }

  @Test
  public void shouldToggleAvailability() {
    Courier courier = new Courier();
    courier.noteAvailable();
    assertTrue(courier.isAvailable());
    courier.noteUnavailable();
    assertFalse(courier.isAvailable());
  }

  @Test
  public void shouldInitLocationFromAddress() {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94607", 37.8044, -122.2712);
    Courier courier = new Courier(new PersonName("John", "Doe"), address);
    assertEquals(37.8044, courier.getCurrentLatitude(), 0.0001);
    assertEquals(-122.2712, courier.getCurrentLongitude(), 0.0001);
  }

  @Test
  public void shouldUpdateLocation() {
    Courier courier = new Courier();
    courier.updateLocation(40.7128, -74.0060);
    assertEquals(40.7128, courier.getCurrentLatitude(), 0.0001);
    assertEquals(-74.0060, courier.getCurrentLongitude(), 0.0001);
    assertNotNull(courier.getLastLocationUpdate());
  }

  @Test
  public void shouldTrackActiveDeliveryCount() {
    Courier courier = new Courier();
    Order order = makeTestOrder();
    courier.addAction(Action.makePickup(order));
    courier.addAction(Action.makeDropoff(order, LocalDateTime.now().plusMinutes(30)));
    assertEquals(1, courier.getActiveDeliveryCount());
  }

  @Test
  public void shouldHaveZeroDeliveriesWhenEmpty() {
    Courier courier = new Courier();
    assertEquals(0, courier.getActiveDeliveryCount());
  }

  @Test
  public void shouldReturnActionsForDelivery() {
    Courier courier = new Courier();
    Order order = makeTestOrder();
    Action pickup = Action.makePickup(order);
    Action dropoff = Action.makeDropoff(order, LocalDateTime.now().plusMinutes(30));
    courier.addAction(pickup);
    courier.addAction(dropoff);

    List<Action> actions = courier.actionsForDelivery(order);
    assertEquals(2, actions.size());
  }

  @Test
  public void shouldCancelDelivery() {
    Courier courier = new Courier();
    Order order = makeTestOrder();
    courier.addAction(Action.makePickup(order));
    courier.addAction(Action.makeDropoff(order, LocalDateTime.now().plusMinutes(30)));

    courier.cancelDelivery(order);
    assertEquals(0, courier.actionsForDelivery(order).size());
    assertEquals(0, courier.getActiveDeliveryCount());
  }

  @Test
  public void hasLocationReturnsFalseWhenNotSet() {
    Courier courier = new Courier();
    assertFalse(courier.hasLocation());
  }

  private Order makeTestOrder() {
    RestaurantMenu menu = new RestaurantMenu(Collections.emptyList());
    Restaurant restaurant = new Restaurant(1L, "Test", menu);
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(100L);
    return order;
  }
}
