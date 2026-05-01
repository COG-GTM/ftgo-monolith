package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

  private OrderRepository orderRepository;
  private RestaurantRepository restaurantRepository;
  private ConsumerService consumerService;
  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;
  private OrderService orderService;

  private Restaurant restaurant;
  private Courier courier;

  @Before
  public void setUp() {
    orderRepository = mock(OrderRepository.class);
    restaurantRepository = mock(RestaurantRepository.class);
    consumerService = mock(ConsumerService.class);
    courierRepository = mock(CourierRepository.class);
    courierAssignmentStrategy = mock(CourierAssignmentStrategy.class);

    orderService = new OrderService(orderRepository, restaurantRepository,
            Optional.empty(), consumerService, courierRepository, courierAssignmentStrategy);

    Address addr = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712);
    MenuItem menuItem = new MenuItem("1", "Chicken Vindaloo", new Money("12.34"));
    restaurant = new Restaurant("Ajanta", addr,
            new RestaurantMenu(Collections.singletonList(menuItem)));
    restaurant.setId(1L);

    courier = new Courier(new PersonName("John", "Doe"), addr);
    courier.noteAvailable();
    courier.updateLocation(37.8050, -122.2720);
  }

  @Test
  public void shouldCreateOrder() {
    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      Order o = invocation.getArgument(0);
      o.setId(1L);
      return o;
    });

    Order order = orderService.createOrder(1L, 1L,
            Collections.singletonList(new MenuItemIdAndQuantity("1", 2)));

    assertNotNull(order);
    assertEquals(OrderState.APPROVED, order.getOrderState());
    verify(consumerService).validateOrderForConsumer(eq(1L), any(Money.class));
    verify(orderRepository).save(any(Order.class));
  }

  @Test(expected = RestaurantNotFoundException.class)
  public void shouldThrowWhenRestaurantNotFound() {
    when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

    orderService.createOrder(1L, 999L,
            Collections.singletonList(new MenuItemIdAndQuantity("1", 2)));
  }

  @Test
  public void shouldCancelOrder() {
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(1L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    Order cancelled = orderService.cancel(1L);
    assertEquals(OrderState.CANCELLED, cancelled.getOrderState());
  }

  @Test(expected = OrderNotFoundException.class)
  public void shouldThrowWhenOrderNotFoundOnCancel() {
    when(orderRepository.findById(999L)).thenReturn(Optional.empty());
    orderService.cancel(999L);
  }

  @Test
  public void shouldAcceptAndScheduleDelivery() {
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(1L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(courierRepository.findAllAvailable()).thenReturn(Collections.singletonList(courier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(courier);

    orderService.accept(1L, LocalDateTime.now().plusHours(1));

    assertEquals(OrderState.ACCEPTED, order.getOrderState());
    assertNotNull(order.getAssignedCourier());
  }

  @Test
  public void shouldTransitionThroughPreparing() {
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.notePreparing(1L);
    assertEquals(OrderState.PREPARING, order.getOrderState());
  }

  @Test
  public void shouldTransitionThroughReadyForPickup() {
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.noteReadyForPickup(1L);
    assertEquals(OrderState.READY_FOR_PICKUP, order.getOrderState());
  }

  @Test
  public void shouldTransitionThroughPickedUp() {
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.notePickedUp(1L);
    assertEquals(OrderState.PICKED_UP, order.getOrderState());
  }

  @Test
  public void shouldTransitionThroughDelivered() {
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.noteDelivered(1L);
    assertEquals(OrderState.DELIVERED, order.getOrderState());
  }

  @Test
  public void shouldEstimateDeliveryTimeWithLocation() {
    Order order = new Order(1L, restaurant, Collections.emptyList());
    order.setId(1L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(courierRepository.findAllAvailable()).thenReturn(Collections.singletonList(courier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(courier);

    orderService.accept(1L, LocalDateTime.now().plusHours(1));

    assertEquals(OrderState.ACCEPTED, order.getOrderState());
    assertNotNull(order.getAssignedCourier());
    assertTrue(courier.getActiveDeliveryCount() > 0);
  }

  @Test
  public void shouldFallbackDeliveryTimeWithoutLocation() {
    Address noLocAddr = new Address("1 Main St", null, "Oakland", "CA", "94612");
    Restaurant noLocRestaurant = new Restaurant("Test", noLocAddr,
            new RestaurantMenu(Collections.emptyList()));
    noLocRestaurant.setId(2L);

    Courier noLocCourier = new Courier(new PersonName("Jane", "Doe"), noLocAddr);
    noLocCourier.noteAvailable();

    Order order = new Order(1L, noLocRestaurant, Collections.emptyList());
    order.setId(2L);
    when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
    when(courierRepository.findAllAvailable()).thenReturn(Collections.singletonList(noLocCourier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(noLocCourier);

    orderService.accept(2L, LocalDateTime.now().plusHours(1));

    assertEquals(OrderState.ACCEPTED, order.getOrderState());
    assertNotNull(order.getAssignedCourier());
  }
}
