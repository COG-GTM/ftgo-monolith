package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Action;
import net.chrisrichardson.ftgo.domain.ActionType;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.DistanceOptimizedCourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.NoCourierAvailableException;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_QUANTITY;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CONSUMER_ID;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.ORDER_ID;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.chickenVindalooLineItems;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_ID;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT_MENU_ITEMS;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT_NAME;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.CHICKEN_VINDALOO;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.CHICKEN_VINDALOO_MENU_ITEM_ID;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.CHICKEN_VINDALOO_PRICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

  private static final double RESTAURANT_LATITUDE = 37.7749;
  private static final double RESTAURANT_LONGITUDE = -122.4194;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private RestaurantRepository restaurantRepository;

  @Mock
  private ConsumerService consumerService;

  @Mock
  private CourierRepository courierRepository;

  @Mock
  private CourierAssignmentStrategy courierAssignmentStrategy;

  private SimpleMeterRegistry meterRegistry;

  private OrderService orderService;

  @Before
  public void setUp() {
    meterRegistry = new SimpleMeterRegistry();
    orderService = new OrderService(orderRepository, restaurantRepository, Optional.of(meterRegistry),
            consumerService, courierRepository, courierAssignmentStrategy);
  }

  // createOrder

  @Test
  public void shouldCreateOrder() {
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.of(AJANTA_RESTAURANT));

    Order order = orderService.createOrder(CONSUMER_ID, AJANTA_ID, CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);

    assertEquals(OrderState.APPROVED, order.getOrderState());
    assertEquals(Long.valueOf(CONSUMER_ID), order.getConsumerId());
    assertSame(AJANTA_RESTAURANT, order.getRestaurant());
    assertEquals(1, order.getLineItems().size());
    assertEquals(CHICKEN_VINDALOO_MENU_ITEM_ID, order.getLineItems().get(0).getMenuItemId());
    assertEquals(CHICKEN_VINDALOO, order.getLineItems().get(0).getName());
    assertEquals(CHICKEN_VINDALOO_PRICE, order.getLineItems().get(0).getPrice());
    assertEquals(CHICKEN_VINDALOO_QUANTITY, order.getLineItems().get(0).getQuantity());
    assertEquals(CHICKEN_VINDALOO_ORDER_TOTAL, order.getOrderTotal());

    ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
    InOrder inOrder = inOrder(consumerService, orderRepository);
    inOrder.verify(consumerService).validateOrderForConsumer(CONSUMER_ID, CHICKEN_VINDALOO_ORDER_TOTAL);
    inOrder.verify(orderRepository).save(savedOrder.capture());
    assertSame(order, savedOrder.getValue());

    assertEquals(1.0, meterRegistry.counter("placed_orders").count(), 0.0);
    assertEquals(1.0, meterRegistry.counter("approved_orders").count(), 0.0);
  }

  @Test
  public void shouldCreateOrderWithoutMeterRegistry() {
    orderService = new OrderService(orderRepository, restaurantRepository, Optional.empty(),
            consumerService, courierRepository, courierAssignmentStrategy);
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.of(AJANTA_RESTAURANT));

    Order order = orderService.createOrder(CONSUMER_ID, AJANTA_ID, CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);

    verify(orderRepository).save(order);
  }

  @Test
  public void shouldThrowWhenRestaurantNotFound() {
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.empty());

    try {
      orderService.createOrder(CONSUMER_ID, AJANTA_ID, CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);
      fail("expected RestaurantNotFoundException");
    } catch (RestaurantNotFoundException e) {
      // expected
    }

    verifyZeroInteractions(consumerService, orderRepository);
    assertEquals(0.0, meterRegistry.counter("placed_orders").count(), 0.0);
  }

  @Test
  public void shouldThrowWhenMenuItemNotFound() {
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.of(AJANTA_RESTAURANT));

    try {
      orderService.createOrder(CONSUMER_ID, AJANTA_ID,
              Collections.singletonList(new MenuItemIdAndQuantity("no-such-item", 1)));
      fail("expected InvalidMenuItemIdException");
    } catch (InvalidMenuItemIdException e) {
      // expected
    }

    verifyZeroInteractions(consumerService, orderRepository);
  }

  @Test
  public void shouldNotSaveOrderWhenConsumerValidationFails() {
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.of(AJANTA_RESTAURANT));
    RuntimeException validationFailure = new RuntimeException("consumer rejected");
    doThrow(validationFailure).when(consumerService).validateOrderForConsumer(CONSUMER_ID, CHICKEN_VINDALOO_ORDER_TOTAL);

    try {
      orderService.createOrder(CONSUMER_ID, AJANTA_ID, CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);
      fail("expected consumer validation failure");
    } catch (RuntimeException e) {
      assertSame(validationFailure, e);
    }

    verify(orderRepository, never()).save(any(Order.class));
    assertEquals(0.0, meterRegistry.counter("placed_orders").count(), 0.0);
  }

  // cancel

  @Test
  public void shouldCancelOrder() {
    Order order = givenOrderInRepository(approvedOrder());

    Order result = orderService.cancel(ORDER_ID);

    assertSame(order, result);
    assertEquals(OrderState.CANCELLED, order.getOrderState());
  }

  @Test
  public void shouldThrowWhenOrderNotFoundOnCancel() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

    try {
      orderService.cancel(ORDER_ID);
      fail("expected OrderNotFoundException");
    } catch (OrderNotFoundException e) {
      // expected
    }
  }

  // accept / scheduleDelivery

  @Test
  public void shouldAcceptAndScheduleDelivery() {
    Order order = givenOrderInRepository(approvedOrder());
    Courier courier = courierWithoutLocation();
    List<Courier> availableCouriers = Arrays.asList(courier, courierWithoutLocation());
    when(courierRepository.findAllAvailable()).thenReturn(availableCouriers);
    when(courierAssignmentStrategy.assignCourier(availableCouriers, order)).thenReturn(courier);
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);

    orderService.accept(ORDER_ID, readyBy);

    assertEquals(OrderState.ACCEPTED, order.getOrderState());
    assertSame(courier, order.getAssignedCourier());

    List<Action> actions = courier.actionsForDelivery(order);
    assertEquals(2, actions.size());
    assertEquals(ActionType.PICKUP, actions.get(0).getType());
    assertNull(actions.get(0).getTime());
    assertEquals(ActionType.DROPOFF, actions.get(1).getType());
    assertEquals(1, courier.getActiveDeliveryCount());

    verify(courierAssignmentStrategy).assignCourier(availableCouriers, order);
    assertEquals(1.0, meterRegistry.counter("courier_assignments").count(), 0.0);
  }

  @Test
  public void shouldThrowWhenOrderNotFoundOnAccept() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

    try {
      orderService.accept(ORDER_ID, LocalDateTime.now().plusHours(1));
      fail("expected OrderNotFoundException");
    } catch (OrderNotFoundException e) {
      // expected
    }

    verifyZeroInteractions(courierRepository, courierAssignmentStrategy);
  }

  @Test
  public void shouldPropagateWhenNoCourierAvailable() {
    Order order = givenOrderInRepository(approvedOrder());
    when(courierRepository.findAllAvailable()).thenReturn(Collections.emptyList());
    when(courierAssignmentStrategy.assignCourier(Collections.emptyList(), order))
            .thenThrow(new NoCourierAvailableException());

    try {
      orderService.accept(ORDER_ID, LocalDateTime.now().plusHours(1));
      fail("expected NoCourierAvailableException");
    } catch (NoCourierAvailableException e) {
      // expected
    }

    assertNull(order.getAssignedCourier());
    assertEquals(0.0, meterRegistry.counter("courier_assignments").count(), 0.0);
  }

  // ticket state transitions

  @Test
  public void shouldTransitionThroughPreparing() {
    Order order = givenOrderInRepository(acceptedOrder());

    orderService.notePreparing(ORDER_ID);

    assertEquals(OrderState.PREPARING, order.getOrderState());
  }

  @Test
  public void shouldTransitionThroughReadyForPickup() {
    Order order = givenOrderInRepository(preparingOrder());

    orderService.noteReadyForPickup(ORDER_ID);

    assertEquals(OrderState.READY_FOR_PICKUP, order.getOrderState());
  }

  @Test
  public void shouldTransitionThroughPickedUp() {
    Order order = givenOrderInRepository(readyForPickupOrder());

    orderService.notePickedUp(ORDER_ID);

    assertEquals(OrderState.PICKED_UP, order.getOrderState());
  }

  @Test
  public void shouldTransitionThroughDelivered() {
    Order order = givenOrderInRepository(pickedUpOrder());

    orderService.noteDelivered(ORDER_ID);

    assertEquals(OrderState.DELIVERED, order.getOrderState());
  }

  @Test
  public void shouldRejectTransitionFromWrongState() {
    Order order = givenOrderInRepository(approvedOrder());

    try {
      orderService.noteReadyForPickup(ORDER_ID);
      fail("expected UnsupportedStateTransitionException");
    } catch (UnsupportedStateTransitionException e) {
      // expected
    }

    assertEquals(OrderState.APPROVED, order.getOrderState());
  }

  @Test
  public void shouldThrowWhenOrderNotFoundOnTransition() {
    when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

    try {
      orderService.notePreparing(ORDER_ID);
      fail("expected OrderNotFoundException");
    } catch (OrderNotFoundException e) {
      // expected
    }
  }

  // delivery time estimation

  @Test
  public void shouldEstimateDeliveryTimeWithLocation() {
    Order order = givenOrderInRepository(approvedOrderFromRestaurantWithLocation());
    Courier courier = courierAt(RESTAURANT_LATITUDE, RESTAURANT_LONGITUDE);
    givenAssignedCourier(order, courier);
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);

    orderService.accept(ORDER_ID, readyBy);

    // Courier is at the restaurant, so it arrives well before readyBy: ETA = readyBy + 15 min
    assertEquals(readyBy.plusMinutes(15), dropoffTime(courier, order));
  }

  @Test
  public void shouldEstimateDeliveryTimeFromCourierArrivalWhenCourierIsFarAway() {
    Order order = givenOrderInRepository(approvedOrderFromRestaurantWithLocation());
    double courierLatitude = RESTAURANT_LATITUDE + 0.2;
    Courier courier = courierAt(courierLatitude, RESTAURANT_LONGITUDE);
    givenAssignedCourier(order, courier);
    long pickupMinutes = (long) DistanceOptimizedCourierAssignmentStrategy.estimateDeliveryMinutes(
            DistanceOptimizedCourierAssignmentStrategy.haversineDistance(
                    courierLatitude, RESTAURANT_LONGITUDE, RESTAURANT_LATITUDE, RESTAURANT_LONGITUDE));
    LocalDateTime readyBy = LocalDateTime.now().plusMinutes(1);
    assertTrue("test setup: courier must arrive after readyBy", pickupMinutes > 1);

    LocalDateTime before = LocalDateTime.now();
    orderService.accept(ORDER_ID, readyBy);
    LocalDateTime after = LocalDateTime.now();

    // Courier arrives after readyBy: ETA = now + pickup travel time + 15 min
    LocalDateTime eta = dropoffTime(courier, order);
    assertFalse(eta.isBefore(before.plusMinutes(pickupMinutes + 15)));
    assertFalse(eta.isAfter(after.plusMinutes(pickupMinutes + 15)));
  }

  @Test
  public void shouldFallbackDeliveryTimeWithoutLocation() {
    Order order = givenOrderInRepository(approvedOrderFromRestaurantWithLocation());
    Courier courier = courierWithoutLocation();
    givenAssignedCourier(order, courier);
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);

    orderService.accept(ORDER_ID, readyBy);

    assertEquals(readyBy.plusMinutes(30), dropoffTime(courier, order));
  }

  @Test
  public void shouldFallbackDeliveryTimeWhenRestaurantHasNoLocation() {
    Order order = givenOrderInRepository(approvedOrder());
    Courier courier = courierAt(RESTAURANT_LATITUDE, RESTAURANT_LONGITUDE);
    givenAssignedCourier(order, courier);
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);

    orderService.accept(ORDER_ID, readyBy);

    assertEquals(readyBy.plusMinutes(30), dropoffTime(courier, order));
  }

  // helpers

  private Order givenOrderInRepository(Order order) {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    return order;
  }

  private void givenAssignedCourier(Order order, Courier courier) {
    List<Courier> availableCouriers = Collections.singletonList(courier);
    when(courierRepository.findAllAvailable()).thenReturn(availableCouriers);
    when(courierAssignmentStrategy.assignCourier(availableCouriers, order)).thenReturn(courier);
  }

  private static LocalDateTime dropoffTime(Courier courier, Order order) {
    return courier.actionsForDelivery(order).stream()
            .filter(a -> a.getType() == ActionType.DROPOFF)
            .findFirst()
            .orElseThrow(() -> new AssertionError("no dropoff action"))
            .getTime();
  }

  private static Order approvedOrder() {
    Restaurant restaurant = new Restaurant(AJANTA_ID, AJANTA_RESTAURANT_NAME, new RestaurantMenu(AJANTA_RESTAURANT_MENU_ITEMS));
    return orderFrom(restaurant);
  }

  private static Order approvedOrderFromRestaurantWithLocation() {
    Address address = new Address("1 Main St", null, "San Francisco", "CA", "94105",
            RESTAURANT_LATITUDE, RESTAURANT_LONGITUDE);
    Restaurant restaurant = new Restaurant(AJANTA_RESTAURANT_NAME, address, new RestaurantMenu(AJANTA_RESTAURANT_MENU_ITEMS));
    restaurant.setId(AJANTA_ID);
    return orderFrom(restaurant);
  }

  private static Order orderFrom(Restaurant restaurant) {
    Order order = new Order(CONSUMER_ID, restaurant, chickenVindalooLineItems());
    order.setId(ORDER_ID);
    return order;
  }

  private static Order acceptedOrder() {
    Order order = approvedOrder();
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    return order;
  }

  private static Order preparingOrder() {
    Order order = acceptedOrder();
    order.notePreparing();
    return order;
  }

  private static Order readyForPickupOrder() {
    Order order = preparingOrder();
    order.noteReadyForPickup();
    return order;
  }

  private static Order pickedUpOrder() {
    Order order = readyForPickupOrder();
    order.notePickedUp();
    return order;
  }

  private static Courier courierWithoutLocation() {
    return new Courier(new PersonName("Jane", "Doe"), new Address("2 Main St", null, "San Francisco", "CA", "94105"));
  }

  private static Courier courierAt(double latitude, double longitude) {
    Courier courier = courierWithoutLocation();
    courier.updateLocation(latitude, longitude);
    return courier;
  }
}
