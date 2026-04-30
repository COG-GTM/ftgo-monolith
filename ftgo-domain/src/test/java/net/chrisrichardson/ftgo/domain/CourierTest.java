package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.Assert.*;

public class CourierTest {

  private Courier courier;
  private Address address;

  @Before
  public void setUp() {
    address = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712);
    courier = new Courier(new PersonName("Jane", "Smith"), address);
  }

  @Test
  public void shouldCreateCourierWithNameAndAddress() {
    assertEquals("Jane", courier.getName().getFirstName());
    assertEquals("Smith", courier.getName().getLastName());
    assertEquals(address, courier.getAddress());
  }

  @Test
  public void shouldInitializeLocationFromAddress() {
    assertEquals(Double.valueOf(37.8044), courier.getCurrentLatitude());
    assertEquals(Double.valueOf(-122.2712), courier.getCurrentLongitude());
  }

  @Test
  public void shouldNotInitializeLocationWhenAddressHasNoCoordinates() {
    Address noCoords = new Address("1 Main St", null, "Oakland", "CA", "94612");
    Courier c = new Courier(new PersonName("Test", "Courier"), noCoords);
    assertNull(c.getCurrentLatitude());
    assertNull(c.getCurrentLongitude());
  }

  @Test
  public void shouldNotBeAvailableByDefault() {
    assertFalse(courier.isAvailable());
  }

  @Test
  public void shouldNoteAvailable() {
    courier.noteAvailable();
    assertTrue(courier.isAvailable());
  }

  @Test
  public void shouldNoteUnavailable() {
    courier.noteAvailable();
    courier.noteUnavailable();
    assertFalse(courier.isAvailable());
  }

  @Test
  public void shouldUpdateLocation() {
    courier.updateLocation(40.7128, -74.0060);
    assertEquals(Double.valueOf(40.7128), courier.getCurrentLatitude());
    assertEquals(Double.valueOf(-74.0060), courier.getCurrentLongitude());
    assertNotNull(courier.getLastLocationUpdate());
  }

  @Test
  public void shouldHaveLocationAfterUpdate() {
    courier.updateLocation(40.7128, -74.0060);
    assertTrue(courier.hasLocation());
  }

  @Test
  public void shouldNotHaveLocationWithNullCoordinates() {
    Address noCoords = new Address("1 Main St", null, "Oakland", "CA", "94612");
    Courier c = new Courier(new PersonName("Test", "Courier"), noCoords);
    assertFalse(c.hasLocation());
  }

  @Test
  public void shouldAddAction() {
    Restaurant restaurant = new Restaurant("Test", address,
            new RestaurantMenu(Collections.emptyList()));
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(100L);

    courier.addAction(Action.makePickup(order));
    assertEquals(1, courier.getPlan().getActions().size());
    assertEquals(ActionType.PICKUP, courier.getPlan().getActions().get(0).getType());
  }

  @Test
  public void shouldTrackActiveDeliveryCountByPickups() {
    Restaurant restaurant = new Restaurant("Test", address,
            new RestaurantMenu(Collections.emptyList()));

    assertEquals(0, courier.getActiveDeliveryCount());

    for (int i = 0; i < 3; i++) {
      Order o = new Order(1L, restaurant, Collections.emptyList());
      o.setId((long) (100 + i));
      courier.addAction(Action.makePickup(o));
    }
    assertEquals(3, courier.getActiveDeliveryCount());
  }

  @Test
  public void shouldCountOnlyPickupsForActiveDeliveries() {
    Restaurant restaurant = new Restaurant("Test", address,
            new RestaurantMenu(Collections.emptyList()));
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(100L);

    courier.addAction(Action.makePickup(order));
    courier.addAction(Action.makeDropoff(order, LocalDateTime.now().plusHours(1)));

    assertEquals(1, courier.getActiveDeliveryCount());
  }

  @Test
  public void shouldCancelDelivery() {
    Restaurant restaurant = new Restaurant("Test", address,
            new RestaurantMenu(Collections.emptyList()));
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(100L);

    courier.addAction(Action.makePickup(order));
    courier.addAction(Action.makeDropoff(order, LocalDateTime.now().plusHours(1)));
    assertEquals(2, courier.getPlan().getActions().size());

    courier.cancelDelivery(order);
    assertEquals(0, courier.getPlan().getActions().size());
  }

  @Test
  public void shouldReturnActionsForSpecificDelivery() {
    Restaurant restaurant = new Restaurant("Test", address,
            new RestaurantMenu(Collections.emptyList()));
    Order order1 = new Order(1L, restaurant, Collections.emptyList());
    order1.setId(100L);
    Order order2 = new Order(1L, restaurant, Collections.emptyList());
    order2.setId(200L);

    courier.addAction(Action.makePickup(order1));
    courier.addAction(Action.makePickup(order2));
    courier.addAction(Action.makeDropoff(order1, LocalDateTime.now().plusHours(1)));

    assertEquals(2, courier.actionsForDelivery(order1).size());
    assertEquals(1, courier.actionsForDelivery(order2).size());
  }

  @Test
  public void shouldHandleMaxCapacityOfFiveDeliveries() {
    Restaurant restaurant = new Restaurant("Test", address,
            new RestaurantMenu(Collections.emptyList()));

    for (int i = 0; i < 5; i++) {
      Order o = new Order(1L, restaurant, Collections.emptyList());
      o.setId((long) (100 + i));
      courier.addAction(Action.makePickup(o));
    }
    assertEquals(5, courier.getActiveDeliveryCount());
  }
}
