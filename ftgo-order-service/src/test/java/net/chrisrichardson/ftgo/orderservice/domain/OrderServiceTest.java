package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.common.Money;
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
  private MeterRegistry meterRegistry;
  private Counter counter;
  private OrderService orderService;

  private Restaurant restaurant;

  @Before
  public void setUp() {
    orderRepository = mock(OrderRepository.class);
    restaurantRepository = mock(RestaurantRepository.class);
    consumerService = mock(ConsumerService.class);
    courierRepository = mock(CourierRepository.class);
    meterRegistry = mock(MeterRegistry.class);
    counter = mock(Counter.class);

    when(meterRegistry.counter(anyString())).thenReturn(counter);

    orderService = new OrderService(orderRepository, restaurantRepository,
        Optional.of(meterRegistry), consumerService, courierRepository);

    restaurant = new Restaurant(1L, "Test Restaurant",
        new RestaurantMenu(Collections.singletonList(
            new MenuItem("item1", "Chicken", new Money("12.00")))));
  }

  @Test
  public void shouldCreateOrder() {
    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      Order o = invocation.getArgument(0);
      o.setId(99L);
      return o;
    });

    Order order = orderService.createOrder(100L, 1L,
        Collections.singletonList(new MenuItemIdAndQuantity("item1", 2)));

    assertNotNull(order);
    assertEquals(OrderState.APPROVED, order.getOrderState());
    verify(orderRepository).save(any(Order.class));
    verify(consumerService).validateOrderForConsumer(eq(100L), any(Money.class));
    verify(counter, times(2)).increment();
  }

  @Test(expected = RestaurantNotFoundException.class)
  public void shouldThrowWhenRestaurantNotFound() {
    when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

    orderService.createOrder(100L, 999L,
        Collections.singletonList(new MenuItemIdAndQuantity("item1", 2)));
  }

  @Test(expected = InvalidMenuItemIdException.class)
  public void shouldThrowWhenMenuItemNotFound() {
    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

    orderService.createOrder(100L, 1L,
        Collections.singletonList(new MenuItemIdAndQuantity("nonexistent", 2)));
  }

  @Test
  public void shouldCancelOrder() {
    Order order = new Order(100L, restaurant,
        Collections.singletonList(new OrderLineItem("item1", "Chicken", new Money("12.00"), 2)));
    order.setId(1L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    Order cancelled = orderService.cancel(1L);

    assertEquals(OrderState.CANCELLED, cancelled.getOrderState());
  }

  @Test(expected = OrderNotFoundException.class)
  public void shouldThrowWhenCancellingNonExistentOrder() {
    when(orderRepository.findById(999L)).thenReturn(Optional.empty());

    orderService.cancel(999L);
  }

  @Test
  public void shouldReviseOrder() {
    Order order = new Order(100L, restaurant,
        Collections.singletonList(new OrderLineItem("item1", "Chicken", new Money("12.00"), 2)));
    order.setId(1L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    OrderRevision revision = new OrderRevision(Optional.empty(),
        Collections.singletonMap("item1", 5));
    Order revised = orderService.reviseOrder(1L, revision);

    assertNotNull(revised);
  }

  @Test
  public void shouldAcceptOrder() {
    Order order = new Order(100L, restaurant,
        Collections.singletonList(new OrderLineItem("item1", "Chicken", new Money("12.00"), 2)));
    order.setId(1L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    Courier courier = mock(Courier.class);
    when(courierRepository.findAllAvailable()).thenReturn(Collections.singletonList(courier));

    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    orderService.accept(1L, readyBy);

    assertEquals(OrderState.ACCEPTED, order.getOrderState());
    assertNotNull(order.getAssignedCourier());
    verify(courier, times(2)).addAction(any(Action.class));
  }

  @Test
  public void shouldNotePreparingOrder() {
    Order order = new Order(100L, restaurant,
        Collections.singletonList(new OrderLineItem("item1", "Chicken", new Money("12.00"), 2)));
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.notePreparing(1L);

    assertEquals(OrderState.PREPARING, order.getOrderState());
  }

  @Test
  public void shouldNoteReadyForPickup() {
    Order order = new Order(100L, restaurant,
        Collections.singletonList(new OrderLineItem("item1", "Chicken", new Money("12.00"), 2)));
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.noteReadyForPickup(1L);

    assertEquals(OrderState.READY_FOR_PICKUP, order.getOrderState());
  }

  @Test
  public void shouldNotePickedUp() {
    Order order = new Order(100L, restaurant,
        Collections.singletonList(new OrderLineItem("item1", "Chicken", new Money("12.00"), 2)));
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.notePickedUp(1L);

    assertEquals(OrderState.PICKED_UP, order.getOrderState());
  }

  @Test
  public void shouldNoteDelivered() {
    Order order = new Order(100L, restaurant,
        Collections.singletonList(new OrderLineItem("item1", "Chicken", new Money("12.00"), 2)));
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
  public void shouldCreateOrderWithoutMeterRegistry() {
    OrderService serviceNoMetrics = new OrderService(orderRepository, restaurantRepository,
        Optional.empty(), consumerService, courierRepository);

    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Order order = serviceNoMetrics.createOrder(100L, 1L,
        Collections.singletonList(new MenuItemIdAndQuantity("item1", 2)));

    assertNotNull(order);
  }
}
