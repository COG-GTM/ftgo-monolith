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

  private static final PersonName NAME = new PersonName("Jane", "Doe");

  private Restaurant restaurant;
  private Courier courier;
  private Order order1;
  private Order order2;

  @Before
  public void setUp() {
    restaurant = new Restaurant("Ajanta",
            new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.emptyList()));
    courier = new Courier(NAME, new Address("2 Main St", null, "Oakland", "CA", "94612"));
    order1 = makeOrder(101L);
    order2 = makeOrder(102L);
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

    courier.noteAvailable();
    assertTrue(courier.isAvailable());
  }

  @Test
  public void shouldInitLocationFromAddress() {
    Courier located = new Courier(NAME,
            new Address("3 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712));

    assertTrue(located.hasLocation());
    assertEquals(37.8044, located.getCurrentLatitude(), 0.0);
    assertEquals(-122.2712, located.getCurrentLongitude(), 0.0);
    assertNull(located.getLastLocationUpdate());
  }

  @Test
  public void shouldUpdateLocation() {
    LocalDateTime before = LocalDateTime.now();

    courier.updateLocation(37.8, -122.4);

    assertTrue(courier.hasLocation());
    assertEquals(37.8, courier.getCurrentLatitude(), 0.0);
    assertEquals(-122.4, courier.getCurrentLongitude(), 0.0);
    assertNotNull(courier.getLastLocationUpdate());
    assertFalse(courier.getLastLocationUpdate().isBefore(before));
  }

  @Test
  public void shouldTrackActiveDeliveryCount() {
    addDelivery(order1);
    assertEquals(1, courier.getActiveDeliveryCount());

    addDelivery(order2);
    assertEquals(2, courier.getActiveDeliveryCount());

    courier.cancelDelivery(order1);
    assertEquals(1, courier.getActiveDeliveryCount());
  }

  @Test
  public void shouldHaveZeroDeliveriesWhenEmpty() {
    assertEquals(0, courier.getActiveDeliveryCount());
    assertTrue(courier.getPlan().getActions().isEmpty());
  }

  @Test
  public void shouldReturnActionsForDelivery() {
    addDelivery(order1);
    addDelivery(order2);

    List<Action> actions = courier.actionsForDelivery(order1);

    assertEquals(2, actions.size());
    assertEquals(ActionType.PICKUP, actions.get(0).getType());
    assertEquals(ActionType.DROPOFF, actions.get(1).getType());
    assertTrue(actions.stream().allMatch(a -> a.actionFor(order1)));
  }

  @Test
  public void shouldCancelDelivery() {
    addDelivery(order1);
    addDelivery(order2);

    courier.cancelDelivery(order1);

    assertTrue(courier.actionsForDelivery(order1).isEmpty());
    assertEquals(2, courier.actionsForDelivery(order2).size());
    assertEquals(2, courier.getPlan().getActions().size());
  }

  @Test
  public void hasLocationReturnsFalseWhenNotSet() {
    assertFalse(courier.hasLocation());
    assertNull(courier.getCurrentLatitude());
    assertNull(courier.getCurrentLongitude());
    assertFalse(new Courier(NAME, null).hasLocation());
    assertFalse(new Courier().hasLocation());
  }

  private Order makeOrder(long id) {
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(id);
    return order;
  }

  private void addDelivery(Order order) {
    courier.addAction(Action.makePickup(order));
    courier.addAction(Action.makeDropoff(order, LocalDateTime.now().plusMinutes(30)));
  }
}
