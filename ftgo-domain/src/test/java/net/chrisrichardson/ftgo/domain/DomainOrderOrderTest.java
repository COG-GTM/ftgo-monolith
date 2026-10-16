package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.CHICKEN_VINDALOO_ID;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.CONSUMER_ID;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.SAMOSA_ID;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.chickenVindaloo;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.money;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.order;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.quantities;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.restaurant;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.revision;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.standardOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;

public class DomainOrderOrderTest {

  private Order order;

  @Before
  public void setUp() {
    order = standardOrder();
  }

  private static LocalDateTime inOneHour() {
    return LocalDateTime.now().plusHours(1);
  }

  private static Order orderIn(OrderState state) {
    Order o = standardOrder();
    switch (state) {
      case APPROVED:
        break;
      case CANCELLED:
        o.cancel();
        break;
      case DELIVERED:
      case PICKED_UP:
      case READY_FOR_PICKUP:
      case PREPARING:
      case ACCEPTED:
        o.acceptTicket(inOneHour());
        if (state == OrderState.ACCEPTED) break;
        o.notePreparing();
        if (state == OrderState.PREPARING) break;
        o.noteReadyForPickup();
        if (state == OrderState.READY_FOR_PICKUP) break;
        o.notePickedUp();
        if (state == OrderState.PICKED_UP) break;
        o.noteDelivered();
        break;
      default:
        throw new IllegalArgumentException(state.name());
    }
    assertThat(o.getOrderState()).isEqualTo(state);
    return o;
  }

  private static void assertRejectedInAllStatesExcept(OrderState allowed, Consumer<Order> transition) {
    for (OrderState state : EnumSet.complementOf(EnumSet.of(allowed))) {
      Order o = orderIn(state);
      try {
        transition.accept(o);
        fail("Expected UnsupportedStateTransitionException in state " + state);
      } catch (UnsupportedStateTransitionException e) {
        assertThat(e).hasMessage("current state: " + state);
        assertThat(o.getOrderState()).isEqualTo(state);
      }
    }
  }

  // --- creation ---

  @Test
  public void newOrderIsApprovedWithGivenConsumerRestaurantAndLineItems() {
    Restaurant restaurant = restaurant();
    Order o = new Order(CONSUMER_ID, restaurant, DomainOrderFixtures.lineItems(chickenVindaloo(2)));

    assertThat(o.getOrderState()).isEqualTo(OrderState.APPROVED);
    assertThat(o.getConsumerId()).isEqualTo(CONSUMER_ID);
    assertThat(o.getRestaurant()).isSameAs(restaurant);
    assertThat(o.getLineItems()).containsExactly(chickenVindaloo(2));
    assertThat(o.getId()).isNull();
    assertThat(o.getVersion()).isNull();
    assertThat(o.getAssignedCourier()).isNull();
  }

  @Test
  public void setIdAssignsIdentity() {
    order.setId(99L);
    assertThat(order.getId()).isEqualTo(99L);
  }

  @Test
  public void orderTotalIsSumOfLineItemTotals() {
    assertThat(order.getOrderTotal()).isEqualTo(money("68.70"));
  }

  @Test
  public void orderTotalOfOrderWithoutLineItemsIsZero() {
    assertThat(new Order(CONSUMER_ID, restaurant(), Collections.emptyList()).getOrderTotal()).isEqualTo(Money.ZERO);
  }

  // --- cancel ---

  @Test
  public void cancelTransitionsApprovedOrderToCancelled() {
    order.cancel();
    assertThat(order.getOrderState()).isEqualTo(OrderState.CANCELLED);
  }

  @Test
  public void cancelIsRejectedInEveryStateOtherThanApproved() {
    assertRejectedInAllStatesExcept(OrderState.APPROVED, Order::cancel);
  }

  // --- acceptTicket ---

  @Test
  public void acceptTicketTransitionsToAcceptedAndRecordsTimes() {
    LocalDateTime readyBy = inOneHour();
    LocalDateTime before = LocalDateTime.now();

    order.acceptTicket(readyBy);

    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
    assertThat(ReflectionTestUtils.getField(order, "readyBy")).isEqualTo(readyBy);
    LocalDateTime acceptTime = (LocalDateTime) ReflectionTestUtils.getField(order, "acceptTime");
    assertThat(acceptTime).isAfterOrEqualTo(before).isBefore(readyBy);
  }

  @Test
  public void acceptTicketRejectsReadyByInThePast() {
    assertThatThrownBy(() -> order.acceptTicket(LocalDateTime.now().minusMinutes(5)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("readyBy is not in the future");
    assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
    assertThat(ReflectionTestUtils.getField(order, "readyBy")).isNull();
  }

  // Suspected bug: acceptTicket assigns acceptTime before validating readyBy, so a rejected
  // acceptance still leaves a stale acceptTime on the (still APPROVED) order.
  @Test
  public void rejectedAcceptTicketCurrentlyLeavesAcceptTimeSet() {
    assertThatThrownBy(() -> order.acceptTicket(LocalDateTime.now().minusMinutes(5)))
            .isInstanceOf(IllegalArgumentException.class);
    assertThat(ReflectionTestUtils.getField(order, "acceptTime")).isNotNull();
  }

  @Test
  public void acceptTicketIsRejectedInEveryStateOtherThanApproved() {
    assertRejectedInAllStatesExcept(OrderState.APPROVED, o -> o.acceptTicket(inOneHour()));
  }

  // --- kitchen / delivery transitions ---

  @Test
  public void notePreparingTransitionsAcceptedOrderToPreparing() {
    Order o = orderIn(OrderState.ACCEPTED);
    o.notePreparing();
    assertThat(o.getOrderState()).isEqualTo(OrderState.PREPARING);
    assertThat(ReflectionTestUtils.getField(o, "preparingTime")).isNotNull();
  }

  @Test
  public void notePreparingIsRejectedInEveryStateOtherThanAccepted() {
    assertRejectedInAllStatesExcept(OrderState.ACCEPTED, Order::notePreparing);
  }

  @Test
  public void noteReadyForPickupTransitionsPreparingOrderToReadyForPickup() {
    Order o = orderIn(OrderState.PREPARING);
    o.noteReadyForPickup();
    assertThat(o.getOrderState()).isEqualTo(OrderState.READY_FOR_PICKUP);
    assertThat(ReflectionTestUtils.getField(o, "readyForPickupTime")).isNotNull();
  }

  @Test
  public void noteReadyForPickupIsRejectedInEveryStateOtherThanPreparing() {
    assertRejectedInAllStatesExcept(OrderState.PREPARING, Order::noteReadyForPickup);
  }

  @Test
  public void notePickedUpTransitionsReadyOrderToPickedUp() {
    Order o = orderIn(OrderState.READY_FOR_PICKUP);
    o.notePickedUp();
    assertThat(o.getOrderState()).isEqualTo(OrderState.PICKED_UP);
    assertThat(ReflectionTestUtils.getField(o, "pickedUpTime")).isNotNull();
  }

  @Test
  public void notePickedUpIsRejectedInEveryStateOtherThanReadyForPickup() {
    assertRejectedInAllStatesExcept(OrderState.READY_FOR_PICKUP, Order::notePickedUp);
  }

  @Test
  public void noteDeliveredTransitionsPickedUpOrderToDelivered() {
    Order o = orderIn(OrderState.PICKED_UP);
    o.noteDelivered();
    assertThat(o.getOrderState()).isEqualTo(OrderState.DELIVERED);
    assertThat(ReflectionTestUtils.getField(o, "deliveredTime")).isNotNull();
  }

  @Test
  public void noteDeliveredIsRejectedInEveryStateOtherThanPickedUp() {
    assertRejectedInAllStatesExcept(OrderState.PICKED_UP, Order::noteDelivered);
  }

  @Test
  public void fullLifecycleReachesDeliveredWithTimestampsInOrder() {
    Order o = orderIn(OrderState.DELIVERED);

    LocalDateTime accept = (LocalDateTime) ReflectionTestUtils.getField(o, "acceptTime");
    LocalDateTime preparing = (LocalDateTime) ReflectionTestUtils.getField(o, "preparingTime");
    LocalDateTime ready = (LocalDateTime) ReflectionTestUtils.getField(o, "readyForPickupTime");
    LocalDateTime pickedUp = (LocalDateTime) ReflectionTestUtils.getField(o, "pickedUpTime");
    LocalDateTime delivered = (LocalDateTime) ReflectionTestUtils.getField(o, "deliveredTime");

    assertThat(preparing).isAfterOrEqualTo(accept);
    assertThat(ready).isAfterOrEqualTo(preparing);
    assertThat(pickedUp).isAfterOrEqualTo(ready);
    assertThat(delivered).isAfterOrEqualTo(pickedUp);
  }

  // --- schedule ---

  @Test
  public void scheduleAssignsCourier() {
    Courier courier = new Courier(new PersonName("Ann", "Courier"), null);
    Order o = orderIn(OrderState.ACCEPTED);
    o.schedule(courier);
    assertThat(o.getAssignedCourier()).isSameAs(courier);
  }

  // Suspected bug: schedule() has no state guard, so a courier can be assigned to a cancelled order.
  @Test
  public void scheduleCurrentlyAssignsCourierToCancelledOrder() {
    Courier courier = new Courier(new PersonName("Ann", "Courier"), null);
    Order o = orderIn(OrderState.CANCELLED);
    o.schedule(courier);
    assertThat(o.getAssignedCourier()).isSameAs(courier);
    assertThat(o.getOrderState()).isEqualTo(OrderState.CANCELLED);
  }

  // --- revise ---

  @Test
  public void reviseUpdatesLineItemQuantitiesAndTotal() {
    order.revise(revision(quantities(CHICKEN_VINDALOO_ID, 1, SAMOSA_ID, 4)));

    assertThat(order.getLineItems()).extracting(OrderLineItem::getQuantity).containsExactly(1, 4);
    assertThat(order.getOrderTotal()).isEqualTo(money("26.34"));
    assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
  }

  @Test
  public void reviseWithOnlyDeliveryInformationReplacesItAndKeepsLineItems() {
    DeliveryInformation newDeliveryInformation = new DeliveryInformation();

    order.revise(new OrderRevision(Optional.of(newDeliveryInformation), Collections.emptyMap()));

    assertThat(ReflectionTestUtils.getField(order, "deliveryInformation")).isSameAs(newDeliveryInformation);
    assertThat(order.getLineItems()).extracting(OrderLineItem::getQuantity).containsExactly(5, 2);
    assertThat(order.getOrderTotal()).isEqualTo(money("68.70"));
  }

  @Test
  public void reviseWithoutDeliveryInformationLeavesItUnchanged() {
    order.revise(revision(quantities(CHICKEN_VINDALOO_ID, 5, SAMOSA_ID, 2)));
    assertThat(ReflectionTestUtils.getField(order, "deliveryInformation")).isNull();
  }

  @Test
  public void reviseIsRejectedInEveryStateOtherThanApproved() {
    assertRejectedInAllStatesExcept(OrderState.APPROVED,
            o -> o.revise(revision(quantities(CHICKEN_VINDALOO_ID, 1, SAMOSA_ID, 1))));
  }

  // Suspected bug: the minimum check in revise() is inverted - it throws OrderMinimumNotMetException
  // when the revised total is GREATER THAN OR EQUAL to orderMinimum (default Integer.MAX_VALUE).
  @Test
  public void reviseCurrentlyThrowsOrderMinimumNotMetWhenNewTotalReachesMinimum() {
    Order o = order(new OrderLineItem("big", "Banquet", new Money(Integer.MAX_VALUE), 1));

    assertThatThrownBy(() -> o.revise(revision(quantities("big", 1))))
            .isInstanceOf(OrderMinimumNotMetException.class);
    assertThat(o.getLineItems().get(0).getQuantity()).isEqualTo(1);
  }

  // Suspected bug: because the minimum check is inverted, a revision that drops the total to zero
  // (well below any sensible minimum) is accepted.
  @Test
  public void reviseCurrentlyAllowsReducingTotalToZero() {
    order.revise(revision(quantities(CHICKEN_VINDALOO_ID, 0, SAMOSA_ID, 0)));
    assertThat(order.getOrderTotal()).isEqualTo(money("0.00"));
  }

  // Suspected bug: revise() does not validate quantities, so negative quantities produce a negative total.
  @Test
  public void reviseCurrentlyAcceptsNegativeQuantities() {
    order.revise(revision(quantities(CHICKEN_VINDALOO_ID, -1, SAMOSA_ID, 0)));
    assertThat(order.getOrderTotal()).isEqualTo(money("-12.34"));
  }

  // Suspected bug: a revision that omits any existing line item fails with a NullPointerException
  // (OrderLineItems.updateLineItems unboxes a missing quantity), and the new delivery information
  // has already been applied by then, so the order is left partially revised.
  @Test
  public void revisePartialLineItemsCurrentlyThrowsNullPointerAfterApplyingDeliveryInformation() {
    DeliveryInformation newDeliveryInformation = new DeliveryInformation();

    assertThatThrownBy(() -> order.revise(
            new OrderRevision(Optional.of(newDeliveryInformation), quantities(SAMOSA_ID, 4))))
            .isInstanceOf(NullPointerException.class);

    assertThat(ReflectionTestUtils.getField(order, "deliveryInformation")).isSameAs(newDeliveryInformation);
  }

  // Suspected bug: revising an unknown menu item surfaces a raw NoSuchElementException from
  // OrderLineItems.findOrderLineItem (Optional.get()) instead of a domain-specific error.
  @Test
  public void reviseUnknownMenuItemCurrentlyThrowsNoSuchElementException() {
    assertThatThrownBy(() -> order.revise(revision(quantities("unknown", 1))))
            .isInstanceOf(NoSuchElementException.class);
    assertThat(order.getOrderTotal()).isEqualTo(money("68.70"));
  }
}
