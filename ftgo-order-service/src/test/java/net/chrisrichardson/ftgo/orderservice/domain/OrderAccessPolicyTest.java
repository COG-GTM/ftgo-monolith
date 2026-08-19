package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.security.FtgoUserDetails;
import net.chrisrichardson.ftgo.common.security.UserRole;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.Restaurant;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderAccessPolicyTest {

  private static final long CONSUMER_ID = 101L;
  private static final long RESTAURANT_ID = 202L;
  private static final long COURIER_ID = 303L;

  private final OrderAccessPolicy policy = new OrderAccessPolicy();

  @After
  public void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateAs(UserRole role, Long subjectId) {
    FtgoUserDetails user = new FtgoUserDetails("user", "secret", true, role, subjectId);
    SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(user, "secret", user.getAuthorities()));
  }

  private Order order() {
    Restaurant restaurant = mock(Restaurant.class);
    when(restaurant.getId()).thenReturn(RESTAURANT_ID);
    Courier courier = mock(Courier.class);
    when(courier.getId()).thenReturn(COURIER_ID);
    Order order = mock(Order.class);
    when(order.getConsumerId()).thenReturn(CONSUMER_ID);
    when(order.getRestaurant()).thenReturn(restaurant);
    when(order.getAssignedCourier()).thenReturn(courier);
    return order;
  }

  @Test(expected = AccessDeniedException.class)
  public void anonymousCallerCannotViewOrders() {
    policy.canView(order());
  }

  @Test
  public void consumerCanOnlyViewOwnOrders() {
    authenticateAs(UserRole.CONSUMER, CONSUMER_ID);
    assertTrue(policy.canView(order()));

    SecurityContextHolder.clearContext();
    authenticateAs(UserRole.CONSUMER, CONSUMER_ID + 1);
    assertFalse(policy.canView(order()));
  }

  @Test
  public void restaurantAndCourierCanViewTheirOwnOrders() {
    authenticateAs(UserRole.RESTAURANT, RESTAURANT_ID);
    assertTrue(policy.canView(order()));

    SecurityContextHolder.clearContext();
    authenticateAs(UserRole.COURIER, COURIER_ID);
    assertTrue(policy.canView(order()));

    SecurityContextHolder.clearContext();
    authenticateAs(UserRole.COURIER, COURIER_ID + 1);
    assertFalse(policy.canView(order()));
  }

  @Test(expected = AccessDeniedException.class)
  public void consumerCannotCancelAnotherConsumersOrder() {
    authenticateAs(UserRole.CONSUMER, CONSUMER_ID + 1);
    policy.checkCanChangeOrderContents(order());
  }

  @Test(expected = AccessDeniedException.class)
  public void courierCannotDriveRestaurantTransitions() {
    authenticateAs(UserRole.COURIER, COURIER_ID);
    policy.checkCanPerformRestaurantTransition(order());
  }

  @Test(expected = AccessDeniedException.class)
  public void restaurantCannotMarkOrderDelivered() {
    authenticateAs(UserRole.RESTAURANT, RESTAURANT_ID);
    policy.checkCanPerformCourierTransition(order());
  }

  @Test
  public void adminMayPerformEveryTransition() {
    authenticateAs(UserRole.ADMIN, null);
    Order order = order();
    policy.checkCanChangeOrderContents(order);
    policy.checkCanPerformRestaurantTransition(order);
    policy.checkCanPerformCourierTransition(order);
    assertTrue(policy.canView(order));
  }

  @Test(expected = AccessDeniedException.class)
  public void consumerCannotReadAnotherConsumersOrderHistory() {
    authenticateAs(UserRole.CONSUMER, CONSUMER_ID);
    policy.consumerIdForOrderHistory(CONSUMER_ID + 1);
  }

  @Test
  public void orderHistoryDefaultsToTheAuthenticatedConsumer() {
    authenticateAs(UserRole.CONSUMER, CONSUMER_ID);
    assertEquals(CONSUMER_ID, policy.consumerIdForOrderHistory(null));
  }

  @Test(expected = AccessDeniedException.class)
  public void consumerCannotPlaceAnOrderForAnotherConsumer() {
    authenticateAs(UserRole.CONSUMER, CONSUMER_ID);
    policy.consumerIdForNewOrder(CONSUMER_ID + 1);
  }

  @Test
  public void newOrdersBelongToTheAuthenticatedConsumer() {
    authenticateAs(UserRole.CONSUMER, CONSUMER_ID);
    assertEquals(CONSUMER_ID, policy.consumerIdForNewOrder(0));
  }
}
