package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.security.FtgoUser;
import net.chrisrichardson.ftgo.security.Role;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderAccessPolicyTest {

  private static final long CONSUMER_ID = 15;
  private static final long RESTAURANT_ID = 25;
  private static final long COURIER_ID = 35;
  private final OrderAccessPolicy policy = new OrderAccessPolicy();

  @Test
  public void readIsAllowedForOperatorConsumerRestaurantAndAssignedCourier() {
    Order order = orderWithCourier();
    policy.checkCanRead(user(Role.OPERATOR, null), order);
    policy.checkCanRead(user(Role.CONSUMER, CONSUMER_ID), order);
    policy.checkCanRead(user(Role.RESTAURANT, RESTAURANT_ID), order);
    policy.checkCanRead(user(Role.COURIER, COURIER_ID), order);
  }

  @Test
  public void readIsDeniedForOtherIdentitiesAndUnrelatedRoles() {
    Order order = orderWithCourier();
    assertDenied(() -> policy.checkCanRead(user(Role.CONSUMER, CONSUMER_ID + 1), order));
    assertDenied(() -> policy.checkCanRead(user(Role.RESTAURANT, RESTAURANT_ID + 1), order));
    assertDenied(() -> policy.checkCanRead(user(Role.COURIER, COURIER_ID + 1), order));
    assertDenied(() -> policy.checkCanRead(user(Role.CONSUMER, CONSUMER_ID + 1), orderWithoutCourier()));
  }

  @Test
  public void consumerActionsRequireTheOrderConsumerOrOperator() {
    Order order = orderWithoutCourier();
    policy.checkIsConsumer(user(Role.OPERATOR, null), order);
    policy.checkIsConsumer(user(Role.CONSUMER, CONSUMER_ID), order);
    assertDenied(() -> policy.checkIsConsumer(user(Role.CONSUMER, CONSUMER_ID + 1), order));
    assertDenied(() -> policy.checkIsConsumer(user(Role.RESTAURANT, RESTAURANT_ID), order));
    assertDenied(() -> policy.checkIsConsumer(user(Role.COURIER, COURIER_ID), order));
  }

  @Test
  public void restaurantActionsRequireTheOrderRestaurantOrOperator() {
    Order order = orderWithoutCourier();
    policy.checkIsRestaurant(user(Role.OPERATOR, null), order);
    policy.checkIsRestaurant(user(Role.RESTAURANT, RESTAURANT_ID), order);
    assertDenied(() -> policy.checkIsRestaurant(user(Role.RESTAURANT, RESTAURANT_ID + 1), order));
    assertDenied(() -> policy.checkIsRestaurant(user(Role.CONSUMER, CONSUMER_ID), order));
    assertDenied(() -> policy.checkIsRestaurant(user(Role.COURIER, COURIER_ID), order));
  }

  @Test
  public void pickupAndDeliveryRequireTheAssignedCourierOrOperator() {
    Order order = orderWithCourier();
    policy.checkIsAssignedCourier(user(Role.OPERATOR, null), order);
    policy.checkIsAssignedCourier(user(Role.OPERATOR, null), orderWithoutCourier());
    policy.checkIsAssignedCourier(user(Role.COURIER, COURIER_ID), order);
    assertDenied(() -> policy.checkIsAssignedCourier(user(Role.COURIER, COURIER_ID + 1), order));
    assertDenied(() -> policy.checkIsAssignedCourier(user(Role.CONSUMER, CONSUMER_ID), order));
    assertDenied(() -> policy.checkIsAssignedCourier(user(Role.COURIER, COURIER_ID), orderWithoutCourier()));
  }

  private Order orderWithoutCourier() {
    return new Order(CONSUMER_ID, new Restaurant(RESTAURANT_ID, "restaurant", new RestaurantMenu(Collections.emptyList())),
            Collections.emptyList());
  }

  private Order orderWithCourier() {
    Order order = orderWithoutCourier();
    Courier courier = mock(Courier.class);
    when(courier.getId()).thenReturn(COURIER_ID);
    order.schedule(courier);
    return order;
  }

  private FtgoUser user(Role role, Long entityId) {
    return new FtgoUser(role.name().toLowerCase(), "hash", role, entityId);
  }

  private void assertDenied(Runnable action) {
    assertThatThrownBy(action::run).isInstanceOf(AccessDeniedException.class);
  }
}
