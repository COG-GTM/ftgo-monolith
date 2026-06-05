package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CourierTest {

  private Courier courier;

  @Before
  public void setUp() {
    courier = new Courier(new PersonName("Jane", "Smith"),
            new Address("10 Park Ave", null, "NYC", "NY", "10001", 40.7128, -74.0060));
  }

  @Test
  public void shouldCreateCourierWithLocationFromAddress() {
    assertThat(courier.getCurrentLatitude()).isEqualTo(40.7128);
    assertThat(courier.getCurrentLongitude()).isEqualTo(-74.0060);
    assertThat(courier.getName().getFirstName()).isEqualTo("Jane");
    assertThat(courier.getName().getLastName()).isEqualTo("Smith");
  }

  @Test
  public void shouldCreateCourierWithAddressWithoutCoordinates() {
    Courier c = new Courier(new PersonName("Bob", "Jones"),
            new Address("5 Elm St", null, "LA", "CA", "90001"));
    assertThat(c.getCurrentLatitude()).isNull();
    assertThat(c.getCurrentLongitude()).isNull();
  }

  @Test
  public void shouldCreateCourierWithNullAddress() {
    Courier c = new Courier(new PersonName("Alice", "Wonder"), null);
    assertThat(c.getAddress()).isNull();
    assertThat(c.getCurrentLatitude()).isNull();
  }

  @Test
  public void shouldNoteAvailable() {
    courier.noteAvailable();
    assertThat(courier.isAvailable()).isTrue();
  }

  @Test
  public void shouldNoteUnavailable() {
    courier.noteAvailable();
    courier.noteUnavailable();
    assertThat(courier.isAvailable()).isFalse();
  }

  @Test
  public void shouldNotBeAvailableByDefault() {
    Courier c = new Courier();
    assertThat(c.isAvailable()).isFalse();
  }

  @Test
  public void shouldUpdateLocation() {
    courier.updateLocation(41.0, -73.5);
    assertThat(courier.getCurrentLatitude()).isEqualTo(41.0);
    assertThat(courier.getCurrentLongitude()).isEqualTo(-73.5);
    assertThat(courier.getLastLocationUpdate()).isNotNull();
  }

  @Test
  public void shouldHaveLocationWhenBothCoordsSet() {
    assertThat(courier.hasLocation()).isTrue();
  }

  @Test
  public void shouldNotHaveLocationWhenNullCoords() {
    Courier c = new Courier(new PersonName("X", "Y"), new Address("1", null, "C", "S", "Z"));
    assertThat(c.hasLocation()).isFalse();
  }

  @Test
  public void shouldAddAction() {
    Restaurant restaurant = new Restaurant("R", new Address("1", null, "C", "S", "Z"), new RestaurantMenu(Arrays.asList(new MenuItem("i1", "Pizza", new Money("12.00")))));
    List<OrderLineItem> items = Arrays.asList(new OrderLineItem("i1", "Pizza", new Money("12.00"), 1));
    Order order = new Order(1L, restaurant, items);
    order.setId(100L);

    courier.addAction(Action.makePickup(order));
    assertThat(courier.getPlan().getActions()).hasSize(1);
  }

  @Test
  public void shouldCancelDelivery() {
    Restaurant restaurant = new Restaurant("R", new Address("1", null, "C", "S", "Z"), new RestaurantMenu(Arrays.asList(new MenuItem("i1", "Pizza", new Money("12.00")))));
    List<OrderLineItem> items = Arrays.asList(new OrderLineItem("i1", "Pizza", new Money("12.00"), 1));
    Order order = new Order(1L, restaurant, items);
    order.setId(100L);

    courier.addAction(Action.makePickup(order));
    courier.addAction(Action.makeDropoff(order, LocalDateTime.now().plusHours(1)));
    assertThat(courier.getPlan().getActions()).hasSize(2);

    courier.cancelDelivery(order);
    assertThat(courier.getPlan().getActions()).isEmpty();
  }

  @Test
  public void shouldGetActionsForDelivery() {
    Restaurant restaurant = new Restaurant("R", new Address("1", null, "C", "S", "Z"), new RestaurantMenu(Arrays.asList(new MenuItem("i1", "Pizza", new Money("12.00")))));
    List<OrderLineItem> items = Arrays.asList(new OrderLineItem("i1", "Pizza", new Money("12.00"), 1));
    Order order = new Order(1L, restaurant, items);
    order.setId(100L);

    courier.addAction(Action.makePickup(order));
    courier.addAction(Action.makeDropoff(order, LocalDateTime.now().plusHours(1)));

    List<Action> actions = courier.actionsForDelivery(order);
    assertThat(actions).hasSize(2);
  }

  @Test
  public void shouldGetActiveDeliveryCount() {
    assertThat(courier.getActiveDeliveryCount()).isEqualTo(0);

    Restaurant restaurant = new Restaurant("R", new Address("1", null, "C", "S", "Z"), new RestaurantMenu(Arrays.asList(new MenuItem("i1", "Pizza", new Money("12.00")))));
    List<OrderLineItem> items = Arrays.asList(new OrderLineItem("i1", "Pizza", new Money("12.00"), 1));
    Order order = new Order(1L, restaurant, items);
    order.setId(100L);

    courier.addAction(Action.makePickup(order));
    assertThat(courier.getActiveDeliveryCount()).isEqualTo(1);
  }

  @Test
  public void shouldGetAddress() {
    assertThat(courier.getAddress()).isNotNull();
    assertThat(courier.getAddress().getCity()).isEqualTo("NYC");
  }

  @Test
  public void shouldGetId() {
    assertThat(courier.getId()).isNull();
  }

  @Test
  public void shouldGetLastLocationUpdateInitiallyNull() {
    Courier c = new Courier(new PersonName("A", "B"), new Address("1", null, "C", "S", "Z", 1.0, 2.0));
    assertThat(c.getLastLocationUpdate()).isNull();
  }
}
