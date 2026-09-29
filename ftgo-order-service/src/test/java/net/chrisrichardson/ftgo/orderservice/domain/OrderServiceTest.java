package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
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
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CONSUMER_ID;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.ORDER_ID;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.chickenVindalooLineItems;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_ID;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT_MENU_ITEMS;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT_NAME;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.CHICKEN_VINDALOO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class OrderServiceTest {

  private static final double RESTAURANT_LATITUDE = 37.7749;
  private static final double RESTAURANT_LONGITUDE = -122.4194;

  private OrderRepository orderRepository;
  private RestaurantRepository restaurantRepository;
  private ConsumerService consumerService;
  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;
  private MeterRegistry meterRegistry;

  private OrderService orderService;

  @Before
  public void setUp() {
    orderRepository = mock(OrderRepository.class);
    restaurantRepository = mock(RestaurantRepository.class);
    consumerService = mock(ConsumerService.class);
    courierRepository = mock(CourierRepository.class);
    courierAssignmentStrategy = mock(CourierAssignmentStrategy.class);
    meterRegistry = new SimpleMeterRegistry();

    orderService = new OrderService(orderRepository, restaurantRepository, Optional.of(meterRegistry),
            consumerService, courierRepository, courierAssignmentStrategy);
  }

  @Test
  public void shouldCreateOrder() {
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.of(AJANTA_RESTAURANT));

    Order order = orderService.createOrder(CONSUMER_ID, AJANTA_ID, CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);

    assertEquals(OrderState.APPROVED, order.getOrderState());
    assertEquals(Long.valueOf(CONSUMER_ID), order.getConsumerId());
    assertSame(AJANTA_RESTAURANT, order.getRestaurant());
    assertEquals(CHICKEN_VINDALOO_ORDER_TOTAL, order.getOrderTotal());
    assertEquals(1, order.getLineItems().size());
    assertEquals(CHICKEN_VINDALOO, order.getLineItems().get(0).getName());

    verify(consumerService).validateOrderForConsumer(CONSUMER_ID, CHICKEN_VINDALOO_ORDER_TOTAL);
    verify(orderRepository).save(order);
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
  }

  @Test
  public void shouldNotSaveOrderWhenConsumerValidationFails() {
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.of(AJANTA_RESTAURANT));
    RuntimeException validationFailure = new RuntimeException("consumer not found");
    doThrow(validationFailure)
            .when(consumerService).validateOrderForConsumer(anyLong(), any());

    try {
      orderService.createOrder(CONSUMER_ID, AJANTA_ID, CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);
      fail("expected consumer validation failure");
    } catch (RuntimeException e) {
      assertSame(validationFailure, e);
    }

    verify(orderRepository, never()).save(any(Order.class));
    assertEquals(0.0, meterRegistry.counter("placed_orders").count(), 0.0);
  }

  @Test(expected = InvalidMenuItemIdException.class)
  public void shouldThrowWhenMenuItemIdIsInvalid() {
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.of(AJANTA_RESTAURANT));

    orderService.createOrder(CONSUMER_ID, AJANTA_ID,
            Collections.singletonList(new MenuItemIdAndQuantity("unknown-item", 1)));
  }

  @Test
  public void shouldCancelOrder() {
    Order order = givenOrderInRepository();

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

  @Test
  public void shouldAcceptAndScheduleDelivery() {
    Order order = givenOrderInRepository();
    Courier courier = new Courier(new PersonName("Jane", "Doe"), null);
    List<Courier> availableCouriers = Collections.singletonList(courier);
    when(courierRepository.findAllAvailable()).thenReturn(availableCouriers);
    when(courierAssignmentStrategy.assignCourier(availableCouriers, order)).thenReturn(courier);
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);

    orderService.accept(ORDER_ID, readyBy);

    assertEquals(OrderState.ACCEPTED, order.getOrderState());
    assertSame(courier, order.getAssignedCourier());
    verify(courierAssignmentStrategy).assignCourier(availableCouriers, order);

    List<Action> actions = courier.actionsForDelivery(order);
    assertEquals(2, actions.size());
    assertEquals(ActionType.PICKUP, actions.get(0).getType());
    assertNull(actions.get(0).getTime());
    assertEquals(ActionType.DROPOFF, actions.get(1).getType());
    assertEquals(1, courier.getActiveDeliveryCount());
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
  public void shouldTransitionThroughPreparing() {
    Order order = givenAcceptedOrderInRepository();

    orderService.notePreparing(ORDER_ID);

    assertEquals(OrderState.PREPARING, order.getOrderState());
  }

  @Test
  public void shouldTransitionThroughReadyForPickup() {
    Order order = givenAcceptedOrderInRepository();
    order.notePreparing();

    orderService.noteReadyForPickup(ORDER_ID);

    assertEquals(OrderState.READY_FOR_PICKUP, order.getOrderState());
  }

  @Test
  public void shouldTransitionThroughPickedUp() {
    Order order = givenAcceptedOrderInRepository();
    order.notePreparing();
    order.noteReadyForPickup();

    orderService.notePickedUp(ORDER_ID);

    assertEquals(OrderState.PICKED_UP, order.getOrderState());
  }

  @Test
  public void shouldTransitionThroughDelivered() {
    Order order = givenAcceptedOrderInRepository();
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();

    orderService.noteDelivered(ORDER_ID);

    assertEquals(OrderState.DELIVERED, order.getOrderState());
  }

  @Test(expected = UnsupportedStateTransitionException.class)
  public void shouldRejectPreparingForApprovedOrder() {
    givenOrderInRepository();

    orderService.notePreparing(ORDER_ID);
  }

  @Test
  public void shouldEstimateDeliveryTimeWithLocation() {
    Order order = givenOrderInRepository(restaurantWithLocation());
    Courier courier = new Courier(new PersonName("Jane", "Doe"), null);
    double courierLatitude = RESTAURANT_LATITUDE + 0.1;
    courier.updateLocation(courierLatitude, RESTAURANT_LONGITUDE);
    givenCourierAssigned(order, courier);
    long pickupMinutes = (long) DistanceOptimizedCourierAssignmentStrategy.estimateDeliveryMinutes(
            DistanceOptimizedCourierAssignmentStrategy.haversineDistance(
                    courierLatitude, RESTAURANT_LONGITUDE, RESTAURANT_LATITUDE, RESTAURANT_LONGITUDE));

    LocalDateTime readyBy = LocalDateTime.now().plusMinutes(5);
    LocalDateTime before = LocalDateTime.now();
    orderService.accept(ORDER_ID, readyBy);
    LocalDateTime after = LocalDateTime.now();

    LocalDateTime dropoffTime = dropoffTime(courier, order);
    assertFalse(dropoffTime.isBefore(before.plusMinutes(pickupMinutes + 15)));
    assertFalse(dropoffTime.isAfter(after.plusMinutes(pickupMinutes + 15)));
  }

  @Test
  public void shouldUseReadyByWhenCourierArrivesBeforeOrderIsReady() {
    Order order = givenOrderInRepository(restaurantWithLocation());
    Courier courier = new Courier(new PersonName("Jane", "Doe"), null);
    courier.updateLocation(RESTAURANT_LATITUDE, RESTAURANT_LONGITUDE);
    givenCourierAssigned(order, courier);

    LocalDateTime readyBy = LocalDateTime.now().plusHours(2);
    orderService.accept(ORDER_ID, readyBy);

    assertEquals(readyBy.plusMinutes(15), dropoffTime(courier, order));
  }

  @Test
  public void shouldFallbackDeliveryTimeWithoutLocation() {
    Order order = givenOrderInRepository(restaurantWithLocation());
    Courier courier = new Courier(new PersonName("Jane", "Doe"), null);
    assertFalse(courier.hasLocation());
    givenCourierAssigned(order, courier);

    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    orderService.accept(ORDER_ID, readyBy);

    assertEquals(readyBy.plusMinutes(30), dropoffTime(courier, order));
  }

  @Test
  public void shouldFallbackDeliveryTimeWhenRestaurantHasNoLocation() {
    Order order = givenOrderInRepository();
    Courier courier = new Courier(new PersonName("Jane", "Doe"), null);
    courier.updateLocation(RESTAURANT_LATITUDE, RESTAURANT_LONGITUDE);
    givenCourierAssigned(order, courier);

    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    orderService.accept(ORDER_ID, readyBy);

    assertEquals(readyBy.plusMinutes(30), dropoffTime(courier, order));
  }

  private Order givenOrderInRepository() {
    return givenOrderInRepository(AJANTA_RESTAURANT);
  }

  private Order givenOrderInRepository(Restaurant restaurant) {
    Order order = new Order(CONSUMER_ID, restaurant, chickenVindalooLineItems());
    order.setId(ORDER_ID);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    return order;
  }

  private Order givenAcceptedOrderInRepository() {
    Order order = givenOrderInRepository();
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    return order;
  }

  private void givenCourierAssigned(Order order, Courier courier) {
    List<Courier> couriers = Collections.singletonList(courier);
    when(courierRepository.findAllAvailable()).thenReturn(couriers);
    when(courierAssignmentStrategy.assignCourier(couriers, order)).thenReturn(courier);
  }

  private Restaurant restaurantWithLocation() {
    Address address = new Address("1 Main St", null, "San Francisco", "CA", "94105",
            RESTAURANT_LATITUDE, RESTAURANT_LONGITUDE);
    Restaurant restaurant = new Restaurant(AJANTA_RESTAURANT_NAME, address, new RestaurantMenu(AJANTA_RESTAURANT_MENU_ITEMS));
    restaurant.setId(AJANTA_ID);
    return restaurant;
  }

  private LocalDateTime dropoffTime(Courier courier, Order order) {
    return courier.actionsForDelivery(order).stream()
            .filter(a -> a.getType() == ActionType.DROPOFF)
            .findFirst()
            .orElseThrow(() -> new AssertionError("no dropoff action"))
            .getTime();
  }
}
