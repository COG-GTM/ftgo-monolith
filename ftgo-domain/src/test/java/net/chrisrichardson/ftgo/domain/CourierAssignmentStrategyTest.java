package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class CourierAssignmentStrategyTest {

  private static final long RESTAURANT_ID = 1L;
  private static final String RESTAURANT_NAME = "Test Restaurant";

  private DistanceOptimizedCourierAssignmentStrategy strategy;
  private Address restaurantAddress;
  private Order order;

  @Before
  public void setUp() {
    strategy = new DistanceOptimizedCourierAssignmentStrategy();

    restaurantAddress = new Address("1 Main St", null, "Oakland", "CA", "94612",
            37.8044, -122.2712);

    order = new Order(1L, RESTAURANT_ID, RESTAURANT_NAME, Collections.emptyList());
  }

  @Test(expected = NoCourierAvailableException.class)
  public void shouldThrowWhenNoCouriersAvailable() {
    strategy.assignCourier(Collections.emptyList(), order, restaurantAddress);
  }

  @Test
  public void shouldAssignClosestCourier() {
    Courier nearCourier = makeCourier("Near", 37.8050, -122.2720);
    Courier farCourier = makeCourier("Far", 37.9000, -122.4000);

    Courier assigned = strategy.assignCourier(Arrays.asList(nearCourier, farCourier), order, restaurantAddress);

    assertEquals(nearCourier, assigned);
  }

  @Test
  public void shouldPreferLessLoadedCourierWhenDistanceSimilar() {
    Courier busyCourier = makeCourier("Busy", 37.8045, -122.2713);
    Courier idleCourier = makeCourier("Idle", 37.8046, -122.2714);

    addFakeDeliveries(busyCourier, 4);

    Courier assigned = strategy.assignCourier(Arrays.asList(busyCourier, idleCourier), order, restaurantAddress);

    assertEquals(idleCourier, assigned);
  }

  @Test
  public void shouldFallbackToLoadBalancingWhenNoLocations() {
    Address noLocationAddr = new Address("1 Main St", null, "Oakland", "CA", "94612");
    Order noLocOrder = new Order(1L, RESTAURANT_ID, RESTAURANT_NAME, Collections.emptyList());

    Courier c1 = makeCourierNoLocation("C1");
    Courier c2 = makeCourierNoLocation("C2");
    addFakeDeliveries(c1, 3);

    Courier assigned = strategy.assignCourier(Arrays.asList(c1, c2), noLocOrder, noLocationAddr);

    assertEquals(c2, assigned);
  }

  @Test
  public void shouldSkipCouriersAtMaxCapacity() {
    Courier maxedCourier = makeCourier("Maxed", 37.8045, -122.2713);
    Courier availableCourier = makeCourier("Available", 37.9000, -122.4000);

    addFakeDeliveries(maxedCourier, 5);

    Courier assigned = strategy.assignCourier(Arrays.asList(maxedCourier, availableCourier), order, restaurantAddress);

    assertEquals(availableCourier, assigned);
  }

  @Test
  public void shouldHandleSingleCourier() {
    Courier onlyCourier = makeCourier("Only", 37.8050, -122.2720);

    Courier assigned = strategy.assignCourier(Collections.singletonList(onlyCourier), order, restaurantAddress);

    assertEquals(onlyCourier, assigned);
  }

  @Test
  public void shouldCalculateHaversineDistanceCorrectly() {
    double distance = DistanceOptimizedCourierAssignmentStrategy.haversineDistance(
            37.7749, -122.4194, 37.8044, -122.2712);
    assertTrue("Distance should be roughly 13km (SF to Oakland)", distance > 12 && distance < 15);
  }

  @Test
  public void shouldEstimateDeliveryMinutesCorrectly() {
    double minutes = DistanceOptimizedCourierAssignmentStrategy.estimateDeliveryMinutes(5.0);
    assertTrue("5km delivery should take roughly 15 minutes", minutes > 14 && minutes < 16);
  }

  @Test
  public void shouldAssignAmongMixOfLocatedAndUnlocated() {
    Courier locatedNear = makeCourier("Near", 37.8045, -122.2713);
    Courier unlocated = makeCourierNoLocation("NoLoc");

    Courier assigned = strategy.assignCourier(Arrays.asList(locatedNear, unlocated), order, restaurantAddress);

    assertEquals(locatedNear, assigned);
  }

  private Courier makeCourier(String name, double lat, double lng) {
    Address addr = new Address("Test St", null, "Oakland", "CA", "94612", lat, lng);
    Courier courier = new Courier(new PersonName(name, "Test"), addr);
    courier.updateLocation(lat, lng);
    courier.noteAvailable();
    return courier;
  }

  private Courier makeCourierNoLocation(String name) {
    Address addr = new Address("Test St", null, "Oakland", "CA", "94612");
    Courier courier = new Courier(new PersonName(name, "Test"), addr);
    courier.noteAvailable();
    return courier;
  }

  private void addFakeDeliveries(Courier courier, int count) {
    for (int i = 0; i < count; i++) {
      Order fakeOrder = new Order(999L, RESTAURANT_ID, RESTAURANT_NAME, Collections.emptyList());
      fakeOrder.setId((long) (1000 + i));
      courier.addAction(Action.makePickup(fakeOrder));
    }
  }
}
