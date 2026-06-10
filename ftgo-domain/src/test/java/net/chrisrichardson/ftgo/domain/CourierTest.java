package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CourierTest {

  private Courier courier;
  private Address address;

  @BeforeEach
  void setUp() {
    address = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712);
    courier = new Courier(new PersonName("John", "Doe"), address);
  }

  @Test
  void shouldCreateCourierWithAddress() {
    assertThat(courier.getName().getFirstName()).isEqualTo("John");
    assertThat(courier.getName().getLastName()).isEqualTo("Doe");
    assertThat(courier.getAddress()).isEqualTo(address);
    assertThat(courier.getCurrentLatitude()).isEqualTo(37.8044);
    assertThat(courier.getCurrentLongitude()).isEqualTo(-122.2712);
  }

  @Test
  void shouldCreateCourierWithoutLocation() {
    Address noLocAddr = new Address("1 Main St", null, "Oakland", "CA", "94612");
    Courier noLocCourier = new Courier(new PersonName("Jane", "Doe"), noLocAddr);

    assertThat(noLocCourier.getCurrentLatitude()).isNull();
    assertThat(noLocCourier.getCurrentLongitude()).isNull();
  }

  @Test
  void shouldCreateCourierWithNullAddress() {
    Courier nullAddrCourier = new Courier(new PersonName("Test", "User"), null);

    assertThat(nullAddrCourier.getAddress()).isNull();
    assertThat(nullAddrCourier.getCurrentLatitude()).isNull();
  }

  @Test
  void shouldDefaultToNotAvailable() {
    assertThat(courier.isAvailable()).isFalse();
  }

  @Test
  void shouldNoteAvailable() {
    courier.noteAvailable();
    assertThat(courier.isAvailable()).isTrue();
  }

  @Test
  void shouldNoteUnavailable() {
    courier.noteAvailable();
    courier.noteUnavailable();
    assertThat(courier.isAvailable()).isFalse();
  }

  @Test
  void shouldUpdateLocation() {
    courier.updateLocation(37.9000, -122.4000);

    assertThat(courier.getCurrentLatitude()).isEqualTo(37.9000);
    assertThat(courier.getCurrentLongitude()).isEqualTo(-122.4000);
    assertThat(courier.getLastLocationUpdate()).isNotNull();
  }

  @Test
  void shouldHasLocationReturnTrueWhenSet() {
    assertThat(courier.hasLocation()).isTrue();
  }

  @Test
  void shouldHasLocationReturnFalseWhenNotSet() {
    Courier noLocCourier = new Courier(new PersonName("Jane", "Doe"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    assertThat(noLocCourier.hasLocation()).isFalse();
  }

  @Test
  void shouldAddAction() {
    Restaurant restaurant = new Restaurant(1L, "Test", new RestaurantMenu(Collections.emptyList()));
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(1L);

    courier.addAction(Action.makePickup(order));

    assertThat(courier.getPlan().getActions()).hasSize(1);
    assertThat(courier.getPlan().getActions().get(0).getType()).isEqualTo(ActionType.PICKUP);
  }

  @Test
  void shouldCancelDelivery() {
    Restaurant restaurant = new Restaurant(1L, "Test", new RestaurantMenu(Collections.emptyList()));
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(1L);

    courier.addAction(Action.makePickup(order));
    courier.addAction(Action.makeDropoff(order, LocalDateTime.now().plusMinutes(30)));

    courier.cancelDelivery(order);

    assertThat(courier.getPlan().getActions()).isEmpty();
  }

  @Test
  void shouldGetActionsForDelivery() {
    Restaurant restaurant = new Restaurant(1L, "Test", new RestaurantMenu(Collections.emptyList()));
    Order order1 = new Order(1L, restaurant, Collections.emptyList());
    order1.setId(1L);
    Order order2 = new Order(2L, restaurant, Collections.emptyList());
    order2.setId(2L);

    courier.addAction(Action.makePickup(order1));
    courier.addAction(Action.makePickup(order2));
    courier.addAction(Action.makeDropoff(order1, LocalDateTime.now().plusMinutes(30)));

    List<Action> actionsForOrder1 = courier.actionsForDelivery(order1);
    assertThat(actionsForOrder1).hasSize(2);
  }

  @Test
  void shouldGetActiveDeliveryCount() {
    Restaurant restaurant = new Restaurant(1L, "Test", new RestaurantMenu(Collections.emptyList()));
    Order order1 = new Order(1L, restaurant, Collections.emptyList());
    order1.setId(1L);
    Order order2 = new Order(2L, restaurant, Collections.emptyList());
    order2.setId(2L);

    courier.addAction(Action.makePickup(order1));
    courier.addAction(Action.makePickup(order2));
    courier.addAction(Action.makeDropoff(order1, LocalDateTime.now().plusMinutes(30)));

    assertThat(courier.getActiveDeliveryCount()).isEqualTo(2);
  }

  @Test
  void shouldGetZeroActiveDeliveriesForNewCourier() {
    assertThat(courier.getActiveDeliveryCount()).isEqualTo(0);
  }

  @Test
  void shouldGetIdReturnNull() {
    assertThat(courier.getId()).isNull();
  }

  @Test
  void shouldCreateDefaultCourier() {
    Courier defaultCourier = new Courier();
    assertThat(defaultCourier.isAvailable()).isFalse();
    assertThat(defaultCourier.getActiveDeliveryCount()).isEqualTo(0);
  }
}
