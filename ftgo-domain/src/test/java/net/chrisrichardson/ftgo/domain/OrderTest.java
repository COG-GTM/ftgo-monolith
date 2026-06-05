package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrderTest {

  private Restaurant restaurant;
  private List<OrderLineItem> lineItems;
  private Order order;

  @Before
  public void setUp() {
    RestaurantMenu menu = new RestaurantMenu(Arrays.asList(
            new MenuItem("item1", "Burger", new Money("10.00")),
            new MenuItem("item2", "Fries", new Money("5.00"))
    ));
    restaurant = new Restaurant("Test Restaurant", new Address("1 Main", null, "NYC", "NY", "10001"), menu);
    lineItems = Arrays.asList(
            new OrderLineItem("item1", "Burger", new Money("10.00"), 2),
            new OrderLineItem("item2", "Fries", new Money("5.00"), 1)
    );
    order = new Order(1L, restaurant, lineItems);
  }

  @Test
  public void shouldCreateOrderWithApprovedState() {
    assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
  }

  @Test
  public void shouldGetConsumerId() {
    assertThat(order.getConsumerId()).isEqualTo(1L);
  }

  @Test
  public void shouldGetRestaurant() {
    assertThat(order.getRestaurant()).isEqualTo(restaurant);
  }

  @Test
  public void shouldGetLineItems() {
    assertThat(order.getLineItems()).hasSize(2);
  }

  @Test
  public void shouldCalculateOrderTotal() {
    // 2 * 10.00 + 1 * 5.00 = 25.00
    assertThat(order.getOrderTotal()).isEqualTo(new Money("25.00"));
  }

  @Test
  public void shouldCancelApprovedOrder() {
    order.cancel();
    assertThat(order.getOrderState()).isEqualTo(OrderState.CANCELLED);
  }

  @Test
  public void shouldNotCancelNonApprovedOrder() {
    order.cancel();
    assertThatThrownBy(() -> order.cancel())
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  public void shouldAcceptTicket() {
    LocalDateTime readyBy = LocalDateTime.now().plusMinutes(30);
    order.acceptTicket(readyBy);
    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
  }

  @Test
  public void shouldNotAcceptTicketIfNotApproved() {
    order.cancel();
    assertThatThrownBy(() -> order.acceptTicket(LocalDateTime.now().plusMinutes(30)))
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  public void shouldNotePreparing() {
    order.acceptTicket(LocalDateTime.now().plusMinutes(30));
    order.notePreparing();
    assertThat(order.getOrderState()).isEqualTo(OrderState.PREPARING);
  }

  @Test
  public void shouldNotNotePreparingIfNotAccepted() {
    assertThatThrownBy(() -> order.notePreparing())
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  public void shouldNoteReadyForPickup() {
    order.acceptTicket(LocalDateTime.now().plusMinutes(30));
    order.notePreparing();
    order.noteReadyForPickup();
    assertThat(order.getOrderState()).isEqualTo(OrderState.READY_FOR_PICKUP);
  }

  @Test
  public void shouldNotNoteReadyForPickupIfNotPreparing() {
    assertThatThrownBy(() -> order.noteReadyForPickup())
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  public void shouldNotePickedUp() {
    order.acceptTicket(LocalDateTime.now().plusMinutes(30));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    assertThat(order.getOrderState()).isEqualTo(OrderState.PICKED_UP);
  }

  @Test
  public void shouldNotNotePickedUpIfNotReady() {
    assertThatThrownBy(() -> order.notePickedUp())
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  public void shouldNoteDelivered() {
    order.acceptTicket(LocalDateTime.now().plusMinutes(30));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    order.noteDelivered();
    assertThat(order.getOrderState()).isEqualTo(OrderState.DELIVERED);
  }

  @Test
  public void shouldNotNoteDeliveredIfNotPickedUp() {
    assertThatThrownBy(() -> order.noteDelivered())
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  public void shouldScheduleCourier() {
    Courier courier = new Courier(new net.chrisrichardson.ftgo.common.PersonName("John", "Doe"),
            new Address("1 Elm", null, "NYC", "NY", "10001"));
    order.schedule(courier);
    assertThat(order.getAssignedCourier()).isEqualTo(courier);
  }

  @Test
  public void shouldSetAndGetId() {
    order.setId(42L);
    assertThat(order.getId()).isEqualTo(42L);
  }

  @Test
  public void shouldGetVersion() {
    assertThat(order.getVersion()).isNull();
  }

  @Test
  public void fullLifecycleHappyPath() {
    order.acceptTicket(LocalDateTime.now().plusMinutes(30));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    order.noteDelivered();
    assertThat(order.getOrderState()).isEqualTo(OrderState.DELIVERED);
  }

  @Test
  public void shouldReviseOrderWithNewQuantities() {
    // Must include all items in the revision map to avoid NPE in updateLineItems
    java.util.Map<String, Integer> revised = new java.util.HashMap<>();
    revised.put("item1", 1);
    revised.put("item2", 1);
    OrderRevision revision = new OrderRevision(java.util.Optional.empty(), revised);

    order.revise(revision);
    assertThat(order.getLineItems().get(0).getQuantity()).isEqualTo(1);
  }

  @Test
  public void shouldReviseOrderWithDeliveryInfo() {
    DeliveryInformation di = new DeliveryInformation();
    java.util.Map<String, Integer> revised = new java.util.HashMap<>();
    OrderRevision revision = new OrderRevision(java.util.Optional.of(di), revised);

    order.revise(revision);
    // No exception means success
  }

  @Test
  public void shouldThrowOnReviseWhenNotApproved() {
    order.cancel();
    java.util.Map<String, Integer> revised = new java.util.HashMap<>();
    OrderRevision revision = new OrderRevision(java.util.Optional.empty(), revised);

    assertThatThrownBy(() -> order.revise(revision))
            .isInstanceOf(UnsupportedStateTransitionException.class);
  }
}
