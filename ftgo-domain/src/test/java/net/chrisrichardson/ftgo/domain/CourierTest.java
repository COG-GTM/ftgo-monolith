package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CourierTest {

  private Restaurant restaurant;
  private Courier courier;

  @Before
  public void setUp() {
    restaurant = new Restaurant("Ajanta", new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.emptyList()));
    courier = new Courier(new PersonName("Jane", "Doe"), new Address("2 Oak St", null, "Oakland", "CA", "94612"));
  }

  @Test
  public void shouldStartUnavailable() {
    assertFalse(courier.isAvailable());
    assertFalse(new Courier().isAvailable());
  }

  @Test
  public void shouldToggleAvailability() {
    courier.noteAvailable();
    assertTrue(courier.isAvailable());

    courier.noteUnavailable();
    assertFalse(courier.isAvailable());
  }

  @Test
  public void shouldInitLocationFromAddress() {
    Courier located = new Courier(new PersonName("John", "Smith"),
            new Address("2 Oak St", null, "Oakland", "CA", "94612", 37.8044, -122.2712));

    assertTrue(located.hasLocation());
    assertEquals(37.8044, located.getCurrentLatitude(), 0.0);
    assertEquals(-122.2712, located.getCurrentLongitude(), 0.0);
    assertNull(located.getLastLocationUpdate());
  }

  @Test
  public void shouldUpdateLocation() {
    LocalDateTime before = LocalDateTime.now();

    courier.updateLocation(37.7749, -122.4194);

    assertTrue(courier.hasLocation());
    assertEquals(37.7749, courier.getCurrentLatitude(), 0.0);
    assertEquals(-122.4194, courier.getCurrentLongitude(), 0.0);
    assertNotNull(courier.getLastLocationUpdate());
    assertFalse(courier.getLastLocationUpdate().isBefore(before));
  }

  @Test
  public void shouldTrackActiveDeliveryCount() {
    Order first = makeOrder(1L);
    Order second = makeOrder(2L);

    courier.addAction(Action.makePickup(first));
    courier.addAction(Action.makeDropoff(first, LocalDateTime.now().plusHours(1)));
    courier.addAction(Action.makePickup(second));

    assertEquals(2, courier.getActiveDeliveryCount());
  }

  @Test
  public void shouldHaveZeroDeliveriesWhenEmpty() {
    assertEquals(0, courier.getActiveDeliveryCount());
  }

  @Test
  public void shouldReturnActionsForDelivery() {
    Order first = makeOrder(1L);
    Order second = makeOrder(2L);
    Action pickup = Action.makePickup(first);
    Action dropoff = Action.makeDropoff(first, LocalDateTime.now().plusHours(1));

    courier.addAction(pickup);
    courier.addAction(Action.makePickup(second));
    courier.addAction(dropoff);

    List<Action> actions = courier.actionsForDelivery(first);

    assertEquals(2, actions.size());
    assertSame(pickup, actions.get(0));
    assertSame(dropoff, actions.get(1));
  }

  @Test
  public void shouldCancelDelivery() {
    Order first = makeOrder(1L);
    Order second = makeOrder(2L);

    courier.addAction(Action.makePickup(first));
    courier.addAction(Action.makeDropoff(first, LocalDateTime.now().plusHours(1)));
    courier.addAction(Action.makePickup(second));

    courier.cancelDelivery(first);

    assertTrue(courier.actionsForDelivery(first).isEmpty());
    assertEquals(1, courier.actionsForDelivery(second).size());
    assertEquals(1, courier.getActiveDeliveryCount());
  }

  @Test
  public void hasLocationReturnsFalseWhenNotSet() {
    assertFalse(courier.hasLocation());
    assertFalse(new Courier(new PersonName("No", "Address"), null).hasLocation());
  }

  private Order makeOrder(long id) {
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(id);
    return order;
  }
}
