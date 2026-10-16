package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class DistanceOptimizedCourierAssignmentStrategyTest {

  private final DistanceOptimizedCourierAssignmentStrategy strategy =
          new DistanceOptimizedCourierAssignmentStrategy();

  @Test
  public void emptyCourierListThrowsExceptionWithAssignmentMessage() {
    try {
      strategy.assignCourier(Collections.<Courier>emptyList(), order(restaurantAt(37.8, -122.2)));
      fail("Expected no-courier exception");
    } catch (NoCourierAvailableException e) {
      assertEquals("No courier available for assignment", e.getMessage());
    }
  }

  @Test
  public void missingRestaurantOrCoordinatesUsesLoadBalancedAssignment() {
    Courier busier = courier("Busier", 37.8, -122.2);
    Courier lessBusy = courier("Less busy", 37.8, -122.2);
    addDeliveries(busier, 2);
    List<Courier> couriers = Arrays.asList(busier, lessBusy);

    assertSame(lessBusy, strategy.assignCourier(couriers, order(null)));
    assertSame(lessBusy, strategy.assignCourier(couriers, order(new Restaurant("No address", null,
            new RestaurantMenu(Collections.<MenuItem>emptyList())))));

    Restaurant latitudeOnly = new Restaurant("Partial coordinates",
            new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8, null),
            new RestaurantMenu(Collections.<MenuItem>emptyList()));
    assertSame(lessBusy, strategy.assignCourier(couriers, order(latitudeOnly)));
  }

  @Test
  public void loadBalancingChoosesCourierWithFewestPickupActions() {
    Courier loaded = courier("Loaded", null, null);
    Courier leastLoaded = courier("Least loaded", null, null);
    addDeliveries(loaded, 3);
    addDeliveries(leastLoaded, 1);

    assertSame(leastLoaded, strategy.assignCourier(Arrays.asList(loaded, leastLoaded),
            order(restaurantAt(37.8, -122.2))));
  }

  @Test
  public void distanceScoreChoosesTheNearerCourier() {
    Courier near = courier("Near", 37.801, -122.201);
    Courier far = courier("Far", 37.9, -122.4);

    assertSame(near, strategy.assignCourier(Arrays.asList(far, near),
            order(restaurantAt(37.8, -122.2))));
  }

  @Test
  public void lowerLoadBreaksTieForCouriersAtTheSameLocation() {
    Courier loaded = courier("Loaded", 37.8, -122.2);
    Courier idle = courier("Idle", 37.8, -122.2);
    addDeliveries(loaded, 2);

    assertSame(idle, strategy.assignCourier(Arrays.asList(loaded, idle),
            order(restaurantAt(37.8, -122.2))));
  }

  @Test
  public void distanceIsCappedAtTwentyKilometersBeforeLoadIsCompared() {
    Courier fartherButIdle = courier("Farther but idle", 40.0, -122.2);
    Courier closerButLoaded = courier("Closer but loaded", 38.0, -122.2);
    addDeliveries(closerButLoaded, 1);

    assertSame(fartherButIdle, strategy.assignCourier(Arrays.asList(closerButLoaded, fartherButIdle),
            order(restaurantAt(37.8, -122.2))));
  }

  @Test
  public void unlocatedCourierCurrentlyBeatsLocatedCourierBeyond20Km() {
    // Suspected bug: the unlocated-courier baseline score is lower than a capped-distance score.
    Courier unlocated = courier("Unlocated", null, null);
    Courier locatedFarAway = courier("Far away", 40.0, -122.2);

    assertSame(unlocated, strategy.assignCourier(Arrays.asList(locatedFarAway, unlocated),
            order(restaurantAt(37.8, -122.2))));
  }

  @Test
  public void assignCourierCurrentlyReturnsOverCapacityCourierWhenAllMaxed() {
    // Suspected bug: load-balance fallback does not enforce MAX_ACTIVE_DELIVERIES.
    Courier atFive = courier("At five", 37.8, -122.2);
    Courier atSix = courier("At six", 37.8, -122.2);
    addDeliveries(atFive, 5);
    addDeliveries(atSix, 6);

    assertSame(atFive, strategy.assignCourier(Arrays.asList(atSix, atFive),
            order(restaurantAt(37.8, -122.2))));
    assertEquals(5, atFive.getActiveDeliveryCount());
  }

  @Test
  public void loadBalancedPathCanReturnCourierAtOrAboveCapacity() {
    Courier atFive = courier("At five", null, null);
    Courier atSix = courier("At six", null, null);
    addDeliveries(atFive, 5);
    addDeliveries(atSix, 6);
    Restaurant withoutCoordinates = new Restaurant("No coordinates",
            new Address("1 Main St", null, "Oakland", "CA", "94612"),
            new RestaurantMenu(Collections.<MenuItem>emptyList()));

    assertSame(atFive, strategy.assignCourier(Arrays.asList(atSix, atFive), order(withoutCoordinates)));
  }

  @Test
  public void haversineDistanceHandlesSamePointSymmetryAndKnownLatitudeDistance() {
    assertEquals(0.0, DistanceOptimizedCourierAssignmentStrategy.haversineDistance(
            37.8, -122.2, 37.8, -122.2), 0.000001);
    double forward = DistanceOptimizedCourierAssignmentStrategy.haversineDistance(0, 0, 1, 0);
    double reverse = DistanceOptimizedCourierAssignmentStrategy.haversineDistance(1, 0, 0, 0);

    assertEquals(111.19, forward, 0.1);
    assertEquals(forward, reverse, 0.000001);
  }

  @Test
  public void estimatesDeliveryMinutesFromDistanceAndBaseTime() {
    assertEquals(5.0, DistanceOptimizedCourierAssignmentStrategy.estimateDeliveryMinutes(0), 0.0);
    assertEquals(25.0, DistanceOptimizedCourierAssignmentStrategy.estimateDeliveryMinutes(10), 0.0);
  }

  private static Restaurant restaurantAt(double latitude, double longitude) {
    return new Restaurant("Restaurant",
            new Address("1 Main St", null, "Oakland", "CA", "94612", latitude, longitude),
            new RestaurantMenu(Collections.<MenuItem>emptyList()));
  }

  private static Order order(Restaurant restaurant) {
    Order order = new Order(1L, restaurant, Collections.<OrderLineItem>emptyList());
    order.setId(1L);
    return order;
  }

  private static Courier courier(String name, Double latitude, Double longitude) {
    Address address = latitude == null || longitude == null
            ? new Address("1 Main St", null, "Oakland", "CA", "94612")
            : new Address("1 Main St", null, "Oakland", "CA", "94612", latitude, longitude);
    return new Courier(new PersonName(name, "Courier"), address);
  }

  private static void addDeliveries(Courier courier, int count) {
    for (int i = 0; i < count; i++) {
      Order delivery = new Order(1L, null, Collections.<OrderLineItem>emptyList());
      delivery.setId((long) i);
      courier.addAction(Action.makePickup(delivery));
    }
  }
}
