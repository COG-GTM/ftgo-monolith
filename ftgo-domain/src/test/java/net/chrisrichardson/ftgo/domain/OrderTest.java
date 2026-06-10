package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

  private Restaurant restaurant;
  private Order order;

  @BeforeEach
  void setUp() {
    restaurant = new Restaurant(1L, "Test Restaurant",
            new RestaurantMenu(Collections.singletonList(
                    new MenuItem("item1", "Chicken", new Money("10.00")))));
    order = new Order(100L, restaurant,
            Collections.singletonList(new OrderLineItem("item1", "Chicken", new Money("10.00"), 2)));
  }

  @Test
  void shouldCreateOrderInApprovedState() {
    assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
    assertThat(order.getConsumerId()).isEqualTo(100L);
    assertThat(order.getRestaurant()).isEqualTo(restaurant);
    assertThat(order.getLineItems()).hasSize(1);
  }

  @Test
  void shouldCalculateOrderTotal() {
    assertThat(order.getOrderTotal()).isEqualTo(new Money("20.00"));
  }

  @Test
  void shouldCancelApprovedOrder() {
    order.cancel();
    assertThat(order.getOrderState()).isEqualTo(OrderState.CANCELLED);
  }

  @Test
  void shouldNotCancelNonApprovedOrder() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));

    assertThatThrownBy(() -> order.cancel())
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  void shouldAcceptTicketWithFutureReadyBy() {
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    order.acceptTicket(readyBy);

    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
  }

  @Test
  void shouldThrowWhenAcceptingNonApprovedOrder() {
    order.cancel();

    assertThatThrownBy(() -> order.acceptTicket(LocalDateTime.now().plusHours(1)))
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  void shouldTransitionToPreparingFromAccepted() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();

    assertThat(order.getOrderState()).isEqualTo(OrderState.PREPARING);
  }

  @Test
  void shouldNotPrepareFromApproved() {
    assertThatThrownBy(() -> order.notePreparing())
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  void shouldTransitionToReadyForPickupFromPreparing() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();

    assertThat(order.getOrderState()).isEqualTo(OrderState.READY_FOR_PICKUP);
  }

  @Test
  void shouldNotReadyForPickupFromAccepted() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));

    assertThatThrownBy(() -> order.noteReadyForPickup())
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  void shouldTransitionToPickedUpFromReadyForPickup() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();

    assertThat(order.getOrderState()).isEqualTo(OrderState.PICKED_UP);
  }

  @Test
  void shouldNotPickUpFromPreparing() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();

    assertThatThrownBy(() -> order.notePickedUp())
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  void shouldTransitionToDeliveredFromPickedUp() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    order.noteDelivered();

    assertThat(order.getOrderState()).isEqualTo(OrderState.DELIVERED);
  }

  @Test
  void shouldNotDeliverFromReadyForPickup() {
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();

    assertThatThrownBy(() -> order.noteDelivered())
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  void shouldSetAndGetId() {
    order.setId(42L);
    assertThat(order.getId()).isEqualTo(42L);
  }

  @Test
  void shouldGetVersion() {
    assertThat(order.getVersion()).isNull();
  }

  @Test
  void shouldScheduleWithCourier() {
    Courier courier = new Courier();
    order.schedule(courier);

    assertThat(order.getAssignedCourier()).isEqualTo(courier);
  }

  @Test
  void shouldReviseOrderWithNewQuantities() {
    Order bigOrder = new Order(100L, restaurant,
            Collections.singletonList(new OrderLineItem("item1", "Chicken", new Money("10.00"), 5)));

    Map<String, Integer> revisedQuantities = new HashMap<>();
    revisedQuantities.put("item1", 3);
    OrderRevision revision = new OrderRevision(Optional.empty(), revisedQuantities);

    bigOrder.revise(revision);

    assertThat(bigOrder.getLineItems().get(0).getQuantity()).isEqualTo(3);
  }

  @Test
  void shouldReviseOrderWithDeliveryInformation() {
    Order bigOrder = new Order(100L, restaurant,
            Collections.singletonList(new OrderLineItem("item1", "Chicken", new Money("10.00"), 5)));

    DeliveryInformation di = new DeliveryInformation();
    Map<String, Integer> revisedQuantities = new HashMap<>();
    revisedQuantities.put("item1", 3);
    OrderRevision revision = new OrderRevision(Optional.of(di), revisedQuantities);

    bigOrder.revise(revision);

    assertThat(bigOrder.getLineItems().get(0).getQuantity()).isEqualTo(3);
  }

  @Test
  void shouldThrowWhenRevisingNonApprovedOrder() {
    order.cancel();

    Map<String, Integer> revisedQuantities = new HashMap<>();
    revisedQuantities.put("item1", 3);
    OrderRevision revision = new OrderRevision(Optional.empty(), revisedQuantities);

    assertThatThrownBy(() -> order.revise(revision))
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  void shouldThrowWhenReadyByIsNotInFuture() {
    assertThatThrownBy(() -> order.acceptTicket(LocalDateTime.now().minusHours(1)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("readyBy is not in the future");
  }

  @Test
  void shouldCompleteFullOrderLifecycle() {
    assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);

    order.acceptTicket(LocalDateTime.now().plusHours(1));
    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);

    order.notePreparing();
    assertThat(order.getOrderState()).isEqualTo(OrderState.PREPARING);

    order.noteReadyForPickup();
    assertThat(order.getOrderState()).isEqualTo(OrderState.READY_FOR_PICKUP);

    order.notePickedUp();
    assertThat(order.getOrderState()).isEqualTo(OrderState.PICKED_UP);

    order.noteDelivered();
    assertThat(order.getOrderState()).isEqualTo(OrderState.DELIVERED);
  }
}
