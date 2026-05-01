package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CourierTest {

  private Courier courier;
  private Address addressWithLocation;
  private Address addressWithoutLocation;

  @Before
  public void setUp() {
    addressWithLocation = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712);
    addressWithoutLocation = new Address("1 Main St", null, "Oakland", "CA", "94612");
  }

  @Test
  public void shouldStartUnavailable() {
    courier = new Courier(new PersonName("John", "Doe"), addressWithLocation);
    assertFalse(courier.isAvailable());
  }

  @Test
  public void shouldToggleAvailability() {
    courier = new Courier(new PersonName("John", "Doe"), addressWithLocation);
    assertFalse(courier.isAvailable());

    courier.noteAvailable();
    assertTrue(courier.isAvailable());

    courier.noteUnavailable();
    assertFalse(courier.isAvailable());
  }

  @Test
  public void shouldInitLocationFromAddress() {
    courier = new Courier(new PersonName("John", "Doe"), addressWithLocation);
    assertEquals(37.8044, courier.getCurrentLatitude(), 0.0001);
    assertEquals(-122.2712, courier.getCurrentLongitude(), 0.0001);
  }

  @Test
  public void shouldUpdateLocation() {
    courier = new Courier(new PersonName("John", "Doe"), addressWithLocation);
    courier.updateLocation(40.7128, -74.0060);

    assertEquals(40.7128, courier.getCurrentLatitude(), 0.0001);
    assertEquals(-74.0060, courier.getCurrentLongitude(), 0.0001);
    assertNotNull(courier.getLastLocationUpdate());
  }

  @Test
  public void shouldTrackActiveDeliveryCount() {
    courier = new Courier(new PersonName("John", "Doe"), addressWithLocation);
    courier.noteAvailable();

    Restaurant restaurant = new Restaurant("Test", addressWithLocation,
            new RestaurantMenu(Collections.emptyList()));
    Order order1 = new Order(1L, restaurant, Collections.emptyList());
    order1.setId(100L);
    Order order2 = new Order(1L, restaurant, Collections.emptyList());
    order2.setId(101L);

    courier.addAction(Action.makePickup(order1));
    courier.addAction(Action.makePickup(order2));

    assertEquals(2, courier.getActiveDeliveryCount());
  }

  @Test
  public void shouldHaveZeroDeliveriesWhenEmpty() {
    courier = new Courier(new PersonName("John", "Doe"), addressWithLocation);
    assertEquals(0, courier.getActiveDeliveryCount());
  }

  @Test
  public void shouldReturnActionsForDelivery() {
    courier = new Courier(new PersonName("John", "Doe"), addressWithLocation);

    Restaurant restaurant = new Restaurant("Test", addressWithLocation,
            new RestaurantMenu(Collections.emptyList()));
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(100L);

    courier.addAction(Action.makePickup(order));
    List<Action> actions = courier.actionsForDelivery(order);

    assertEquals(1, actions.size());
    assertEquals(ActionType.PICKUP, actions.get(0).getType());
  }

  @Test
  public void shouldCancelDelivery() {
    courier = new Courier(new PersonName("John", "Doe"), addressWithLocation);

    Restaurant restaurant = new Restaurant("Test", addressWithLocation,
            new RestaurantMenu(Collections.emptyList()));
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(100L);

    courier.addAction(Action.makePickup(order));
    assertEquals(1, courier.getActiveDeliveryCount());

    courier.cancelDelivery(order);
    assertEquals(0, courier.getActiveDeliveryCount());
  }

  @Test
  public void hasLocationReturnsFalseWhenNotSet() {
    courier = new Courier(new PersonName("John", "Doe"), addressWithoutLocation);
    assertFalse(courier.hasLocation());
  }
}
