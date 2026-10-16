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
  public void startsUnavailableAndCanToggleAvailability() {
    Courier courier = new Courier();

    assertFalse(courier.isAvailable());
    courier.noteAvailable();
    assertTrue(courier.isAvailable());
    courier.noteUnavailable();
    assertFalse(courier.isAvailable());
  }

  @Test
  public void initializesLocationFromAddressWhenCoordinatesArePresent() {
    Courier courier = courierWithAddress(new Address("1 Main St", null, "Oakland", "CA",
            "94612", 37.8, -122.2));

    assertTrue(courier.hasLocation());
    assertEquals(Double.valueOf(37.8), courier.getCurrentLatitude());
    assertEquals(Double.valueOf(-122.2), courier.getCurrentLongitude());
  }

  @Test
  public void doesNotInitializeLocationWhenAddressHasNoCoordinates() {
    Courier courier = courierWithAddress(new Address("1 Main St", null, "Oakland", "CA", "94612"));

    assertFalse(courier.hasLocation());
    assertNull(courier.getCurrentLatitude());
    assertNull(courier.getCurrentLongitude());
  }

  @Test
  public void allowsNullAddressWithoutLocation() {
    Courier courier = courierWithAddress(null);

    assertNull(courier.getAddress());
    assertFalse(courier.hasLocation());
  }

  @Test
  public void updateLocationSetsCoordinatesAndUpdateTime() {
    Courier courier = new Courier();
    LocalDateTime before = LocalDateTime.now();

    courier.updateLocation(37.8, -122.2);

    LocalDateTime after = LocalDateTime.now();
    assertTrue(courier.hasLocation());
    assertEquals(Double.valueOf(37.8), courier.getCurrentLatitude());
    assertEquals(Double.valueOf(-122.2), courier.getCurrentLongitude());
    assertNotNull(courier.getLastLocationUpdate());
    assertFalse(courier.getLastLocationUpdate().isBefore(before));
    assertFalse(courier.getLastLocationUpdate().isAfter(after));
  }

  @Test
  public void countsOnlyPickupActionsAsActiveDeliveries() {
    Courier courier = new Courier();
    Order first = order(1L);
    Order second = order(2L);
    courier.addAction(Action.makePickup(first));
    courier.addAction(Action.makeDropoff(first, LocalDateTime.now()));
    courier.addAction(Action.makePickup(second));

    assertEquals(2, courier.getActiveDeliveryCount());
  }

  @Test
  public void returnsPickupAndDropoffActionsForOnlyTheRequestedDelivery() {
    Courier courier = new Courier();
    Order requested = order(1L);
    courier.addAction(Action.makePickup(requested));
    courier.addAction(Action.makeDropoff(requested, LocalDateTime.now()));
    courier.addAction(Action.makePickup(order(2L)));

    List<Action> actions = courier.actionsForDelivery(order(1L));

    assertEquals(2, actions.size());
    assertEquals(ActionType.PICKUP, actions.get(0).getType());
    assertEquals(ActionType.DROPOFF, actions.get(1).getType());
  }

  @Test
  public void cancelDeliveryRemovesItsActionsAndReducesActiveCount() {
    Courier courier = new Courier();
    Order toCancel = order(1L);
    Order toKeep = order(2L);
    courier.addAction(Action.makePickup(toCancel));
    courier.addAction(Action.makeDropoff(toCancel, LocalDateTime.now()));
    courier.addAction(Action.makePickup(toKeep));

    courier.cancelDelivery(order(1L));

    assertTrue(courier.actionsForDelivery(toCancel).isEmpty());
    assertEquals(1, courier.getActiveDeliveryCount());
    assertEquals(1, courier.actionsForDelivery(toKeep).size());
  }

  private static Courier courierWithAddress(Address address) {
    return new Courier(new PersonName("Casey", "Courier"), address);
  }

  private static Order order(Long id) {
    Order order = new Order(1L, null, Collections.emptyList());
    order.setId(id);
    return order;
  }
}
