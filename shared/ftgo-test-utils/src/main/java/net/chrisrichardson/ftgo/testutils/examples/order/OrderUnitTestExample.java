package net.chrisrichardson.ftgo.testutils.examples.order;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderLineItem;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.testutils.builders.OrderBuilder;
import net.chrisrichardson.ftgo.testutils.builders.OrderLineItemBuilder;

import java.time.LocalDateTime;
import java.util.Arrays;

import static net.chrisrichardson.ftgo.testutils.assertions.OrderAssertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Example unit tests for the Order bounded context.
 *
 * <p>Demonstrates how to use test data builders, custom assertions, and
 * JUnit 5 patterns for testing Order domain logic.
 *
 * <p><b>To run these examples as actual tests, copy this class into your
 * service's {@code src/test/java} directory and add JUnit 5 annotations.</b>
 *
 * <h3>Pattern: Domain Entity Unit Test</h3>
 * <p>Tests domain logic (state transitions, calculations) without any framework.
 * These are the fastest tests and should form the bulk of the test suite.
 *
 * <pre>{@code
 * // In ftgo-order-service/src/test/java/.../OrderDomainTest.java:
 *
 * @ExtendWith(MockitoExtension.class)
 * @DisplayName("Order Domain Unit Tests")
 * class OrderDomainTest {
 *
 *     @Test
 *     @DisplayName("should create order in APPROVED state")
 *     void shouldCreateOrderInApprovedState() {
 *         Order order = OrderBuilder.anOrder().build();
 *
 *         assertOrderApproved(order);
 *     }
 *
 *     @Test
 *     @DisplayName("should calculate order total from line items")
 *     void shouldCalculateOrderTotal() {
 *         OrderLineItem item1 = OrderLineItemBuilder.anOrderLineItem()
 *             .withPrice("10.00").withQuantity(2).build();
 *         OrderLineItem item2 = OrderLineItemBuilder.anOrderLineItem()
 *             .withMenuItemId("2").withName("Naan").withPrice("3.00").withQuantity(3).build();
 *
 *         Order order = OrderBuilder.anOrder()
 *             .withLineItems(item1, item2)
 *             .build();
 *
 *         // 10.00 * 2 + 3.00 * 3 = 29.00
 *         assertOrderTotal(order, new Money("29.00"));
 *     }
 *
 *     @Test
 *     @DisplayName("should transition from APPROVED to ACCEPTED")
 *     void shouldAcceptApprovedOrder() {
 *         Order order = OrderBuilder.anOrder().build();
 *         LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
 *
 *         order.acceptTicket(readyBy);
 *
 *         assertOrderState(order, OrderState.ACCEPTED);
 *     }
 *
 *     @Test
 *     @DisplayName("should cancel approved order")
 *     void shouldCancelApprovedOrder() {
 *         Order order = OrderBuilder.anOrder().build();
 *
 *         order.cancel();
 *
 *         assertOrderCancelled(order);
 *     }
 *
 *     @Test
 *     @DisplayName("should reject cancel for non-approved order")
 *     void shouldRejectCancelForNonApprovedOrder() {
 *         Order order = OrderBuilder.anOrder().build();
 *         order.acceptTicket(LocalDateTime.now().plusHours(1));
 *
 *         assertThatThrownBy(order::cancel)
 *             .isInstanceOf(UnsupportedStateTransitionException.class);
 *     }
 *
 *     @Test
 *     @DisplayName("should follow full lifecycle: APPROVED -> ACCEPTED -> PREPARING -> READY -> PICKED_UP -> DELIVERED")
 *     void shouldFollowFullLifecycle() {
 *         Order order = OrderBuilder.anOrder().build();
 *
 *         order.acceptTicket(LocalDateTime.now().plusHours(1));
 *         assertOrderState(order, OrderState.ACCEPTED);
 *
 *         order.notePreparing();
 *         assertOrderState(order, OrderState.PREPARING);
 *
 *         order.noteReadyForPickup();
 *         assertOrderState(order, OrderState.READY_FOR_PICKUP);
 *
 *         order.notePickedUp();
 *         assertOrderState(order, OrderState.PICKED_UP);
 *
 *         order.noteDelivered();
 *         assertOrderState(order, OrderState.DELIVERED);
 *     }
 *
 *     @Test
 *     @DisplayName("should track consumer ID")
 *     void shouldTrackConsumerId() {
 *         long consumerId = 42L;
 *         Order order = OrderBuilder.anOrder()
 *             .withConsumerId(consumerId)
 *             .build();
 *
 *         assertOrderBelongsToConsumer(order, consumerId);
 *     }
 *
 *     @Test
 *     @DisplayName("should have correct line item count")
 *     void shouldHaveCorrectLineItemCount() {
 *         Order order = OrderBuilder.anOrder()
 *             .withLineItems(
 *                 OrderLineItemBuilder.anOrderLineItem().build(),
 *                 OrderLineItemBuilder.anOrderLineItem().withMenuItemId("2").build()
 *             )
 *             .build();
 *
 *         assertOrderLineItemCount(order, 2);
 *     }
 * }
 * }</pre>
 */
public final class OrderUnitTestExample {
    private OrderUnitTestExample() {
        // Documentation-only class
    }
}
