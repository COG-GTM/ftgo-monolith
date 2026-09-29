package net.chrisrichardson.ftgo.orderservice.integration;

import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerConfiguration;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderLineItem;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderRevision;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.orderservice.domain.InvalidMenuItemIdException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.orderservice.domain.OrderServiceWithRepositoriesConfiguration;
import net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_QUANTITY;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.CHICKEN_VINDALOO;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.CHICKEN_VINDALOO_MENU_ITEM_ID;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.CHICKEN_VINDALOO_PRICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SpringBootTest(classes = {OrderServiceWithRepositoriesConfiguration.class, ConsumerConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class OrderServiceIntegrationTest extends AbstractOrderFlowIntegrationTest {

  @Autowired
  private OrderService orderService;

  @Autowired
  private OrderRepository orderRepository;

  @Test
  public void shouldCreateOrderAndPersistLineItems() {
    long consumerId = createConsumer();
    long restaurantId = createRestaurant();

    long orderId = orderService.createOrder(consumerId, restaurantId, CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES).getId();

    assertEquals("APPROVED", orderStateInDb(orderId));

    transactionTemplate.execute(status -> {
      Order order = orderRepository.findById(orderId).get();
      assertEquals(OrderState.APPROVED, order.getOrderState());
      assertEquals(Long.valueOf(consumerId), order.getConsumerId());
      assertEquals(Long.valueOf(restaurantId), order.getRestaurant().getId());
      assertEquals(Long.valueOf(0), order.getVersion());

      List<OrderLineItem> lineItems = order.getLineItems();
      assertEquals(1, lineItems.size());
      OrderLineItem lineItem = lineItems.get(0);
      assertEquals(CHICKEN_VINDALOO_MENU_ITEM_ID, lineItem.getMenuItemId());
      assertEquals(CHICKEN_VINDALOO, lineItem.getName());
      assertEquals(CHICKEN_VINDALOO_PRICE, lineItem.getPrice());
      assertEquals(CHICKEN_VINDALOO_QUANTITY, lineItem.getQuantity());
      assertEquals(CHICKEN_VINDALOO_ORDER_TOTAL, order.getOrderTotal());
      return null;
    });

    assertEquals(Collections.singletonList(orderId), transactionTemplate.execute(status ->
            orderRepository.findAllByConsumerId(consumerId).stream().map(Order::getId).collect(toList())));
  }

  @Test
  public void shouldNotPersistOrderForUnknownConsumer() {
    long restaurantId = createRestaurant();

    try {
      orderService.createOrder(-1L, restaurantId, CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);
      fail("expected ConsumerNotFoundException");
    } catch (ConsumerNotFoundException e) {
      // expected
    }

    assertEquals(0, countRows("orders"));
    assertEquals(0, countRows("order_line_items"));
  }

  @Test(expected = RestaurantNotFoundException.class)
  public void shouldRejectOrderForUnknownRestaurant() {
    orderService.createOrder(createConsumer(), -1L, CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);
  }

  @Test
  public void shouldRejectOrderForUnknownMenuItem() {
    long consumerId = createConsumer();
    long restaurantId = createRestaurant();

    try {
      orderService.createOrder(consumerId, restaurantId,
              Collections.singletonList(new MenuItemIdAndQuantity("no-such-item", 1)));
      fail("expected InvalidMenuItemIdException");
    } catch (InvalidMenuItemIdException e) {
      // expected
    }

    assertEquals(0, countRows("orders"));
  }

  @Test
  public void shouldTakeOrderThroughFullDeliveryLifecycle() {
    long consumerId = createConsumer();
    long restaurantId = createRestaurant();
    long courierId = createAvailableCourier();
    long orderId = orderService.createOrder(consumerId, restaurantId, CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES).getId();

    orderService.accept(orderId, LocalDateTime.now().plusHours(1));

    assertEquals("ACCEPTED", orderStateInDb(orderId));
    assertEquals(Long.valueOf(courierId), jdbcTemplate.queryForObject(
            "select assigned_courier_id from orders where id = ?", Long.class, orderId));
    assertEquals(Arrays.asList("PICKUP", "DROPOFF"), courierActionTypesInDb(orderId));
    assertNotNull(jdbcTemplate.queryForObject(
            "select time from courier_actions where order_id = ? and type = 'DROPOFF'", Timestamp.class, orderId));

    orderService.notePreparing(orderId);
    assertEquals("PREPARING", orderStateInDb(orderId));

    orderService.noteReadyForPickup(orderId);
    assertEquals("READY_FOR_PICKUP", orderStateInDb(orderId));

    orderService.notePickedUp(orderId);
    assertEquals("PICKED_UP", orderStateInDb(orderId));

    orderService.noteDelivered(orderId);
    assertEquals("DELIVERED", orderStateInDb(orderId));

    Map<String, Object> row = jdbcTemplate.queryForMap(
            "select accept_time, ready_by, preparing_time, ready_for_pickup_time, picked_up_time, delivered_time, version " +
                    "from orders where id = ?", orderId);
    for (String column : Arrays.asList("accept_time", "ready_by", "preparing_time", "ready_for_pickup_time",
            "picked_up_time", "delivered_time")) {
      assertNotNull(column + " should be recorded", row.get(column));
    }
    assertEquals("one version increment per state transition", 5L, ((Number) row.get("version")).longValue());
  }

  @Test
  public void shouldCancelApprovedOrder() {
    long orderId = orderService.createOrder(createConsumer(), createRestaurant(), CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES).getId();

    orderService.cancel(orderId);

    assertEquals("CANCELLED", orderStateInDb(orderId));
  }

  @Test
  public void shouldNotCancelAcceptedOrder() {
    createAvailableCourier();
    long orderId = orderService.createOrder(createConsumer(), createRestaurant(), CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES).getId();
    orderService.accept(orderId, LocalDateTime.now().plusHours(1));

    try {
      orderService.cancel(orderId);
      fail("expected UnsupportedStateTransitionException");
    } catch (UnsupportedStateTransitionException e) {
      // expected
    }

    assertEquals("ACCEPTED", orderStateInDb(orderId));
  }

  @Test
  public void shouldReviseLineItemQuantities() {
    long orderId = orderService.createOrder(createConsumer(), createRestaurant(), CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES).getId();

    orderService.reviseOrder(orderId,
            new OrderRevision(Optional.empty(), Collections.singletonMap(CHICKEN_VINDALOO_MENU_ITEM_ID, 2)));

    assertEquals(Integer.valueOf(2), jdbcTemplate.queryForObject(
            "select quantity from order_line_items where order_id = ? and menu_item_id = ?",
            Integer.class, orderId, CHICKEN_VINDALOO_MENU_ITEM_ID));
    assertEquals(CHICKEN_VINDALOO_PRICE.multiply(2),
            transactionTemplate.execute(status -> orderRepository.findById(orderId).get().getOrderTotal()));
  }

  @Test
  public void shouldRejectStaleUpdateWithOptimisticLocking() {
    long orderId = orderService.createOrder(createConsumer(), createRestaurant(), CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES).getId();
    Order staleCopy = transactionTemplate.execute(status -> orderRepository.findById(orderId).get());

    orderService.cancel(orderId);

    try {
      transactionTemplate.execute(status -> orderRepository.save(staleCopy));
      fail("expected ObjectOptimisticLockingFailureException");
    } catch (ObjectOptimisticLockingFailureException e) {
      // expected
    }

    assertEquals("CANCELLED", orderStateInDb(orderId));
    assertTrue(jdbcTemplate.queryForObject("select version from orders where id = ?", Long.class, orderId) > 0);
  }
}
