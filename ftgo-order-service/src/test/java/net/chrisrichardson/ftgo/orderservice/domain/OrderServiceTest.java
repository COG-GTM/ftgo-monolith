package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

  private OrderService orderService;
  private OrderRepository orderRepository;
  private RestaurantRepository restaurantRepository;
  private MeterRegistry meterRegistry;
  private ConsumerService consumerService;
  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;

  private Restaurant restaurant;
  private static final long CONSUMER_ID = 1L;
  private static final long RESTAURANT_ID = 1L;

  @Before
  public void setUp() {
    orderRepository = mock(OrderRepository.class);
    restaurantRepository = mock(RestaurantRepository.class);
    meterRegistry = mock(MeterRegistry.class);
    consumerService = mock(ConsumerService.class);
    courierRepository = mock(CourierRepository.class);
    courierAssignmentStrategy = mock(CourierAssignmentStrategy.class);

    Counter counter = mock(Counter.class);
    when(meterRegistry.counter(anyString())).thenReturn(counter);

    orderService = new OrderService(orderRepository, restaurantRepository,
            Optional.of(meterRegistry), consumerService, courierRepository, courierAssignmentStrategy);

    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712);
    restaurant = new Restaurant("Ajanta", address,
            new RestaurantMenu(Collections.singletonList(new MenuItem("1", "Chicken Vindaloo", new Money("12.34")))));
    restaurant.setId(RESTAURANT_ID);
  }

  @Test
  public void shouldCreateOrder() {
    when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      Order order = invocation.getArgument(0);
      order.setId(99L);
      return order;
    });

    List<MenuItemIdAndQuantity> lineItems = Collections.singletonList(
            new MenuItemIdAndQuantity("1", 2));

    Order order = orderService.createOrder(CONSUMER_ID, RESTAURANT_ID, lineItems);

    assertNotNull(order);
    assertEquals(OrderState.APPROVED, order.getOrderState());
    assertEquals(new Money("24.68"), order.getOrderTotal());
    verify(consumerService).validateOrderForConsumer(eq(CONSUMER_ID), any(Money.class));
    verify(orderRepository).save(any(Order.class));
  }

  @Test(expected = RestaurantNotFoundException.class)
  public void shouldThrowWhenCreatingOrderWithNonExistentRestaurant() {
    when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

    orderService.createOrder(CONSUMER_ID, 999L, Collections.singletonList(
            new MenuItemIdAndQuantity("1", 1)));
  }

  @Test(expected = ConsumerNotFoundException.class)
  public void shouldThrowWhenConsumerValidationFails() {
    when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
    doThrow(new ConsumerNotFoundException()).when(consumerService)
            .validateOrderForConsumer(anyLong(), any(Money.class));

    orderService.createOrder(CONSUMER_ID, RESTAURANT_ID, Collections.singletonList(
            new MenuItemIdAndQuantity("1", 1)));
  }

  @Test
  public void shouldCancelOrder() {
    Order order = new Order(CONSUMER_ID, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 1)));
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
    OrderLineItem lineItem = new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 2);
    Order order = new Order(CONSUMER_ID, restaurant, Collections.singletonList(lineItem));
    order.setId(1L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    OrderRevision revision = new OrderRevision(Optional.empty(),
            Collections.singletonMap("1", 3));

    Order revised = orderService.reviseOrder(1L, revision);

    assertNotNull(revised);
    assertEquals(3, revised.getLineItems().get(0).getQuantity());
  }

  @Test
  public void shouldScheduleDelivery() {
    Order order = new Order(CONSUMER_ID, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 1)));
    order.setId(1L);

    Courier courier = new Courier(new PersonName("Jane", "Smith"),
            new Address("1 Main St", null, "Oakland", "CA", "94612", 37.8044, -122.2712));
    courier.noteAvailable();

    when(courierRepository.findAllAvailable()).thenReturn(Collections.singletonList(courier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(courier);

    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    orderService.scheduleDelivery(order, readyBy);

    assertSame(courier, order.getAssignedCourier());
    assertEquals(2, courier.getPlan().getActions().size());
  }

  @Test(expected = NoCourierAvailableException.class)
  public void shouldThrowWhenNoCouriersAvailable() {
    Order order = new Order(CONSUMER_ID, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 1)));
    order.setId(1L);

    when(courierRepository.findAllAvailable()).thenReturn(Collections.emptyList());
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class)))
            .thenThrow(new NoCourierAvailableException());

    orderService.scheduleDelivery(order, LocalDateTime.now().plusHours(1));
  }

  @Test
  public void shouldNotePreparing() {
    Order order = new Order(CONSUMER_ID, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 1)));
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.notePreparing(1L);

    assertEquals(OrderState.PREPARING, order.getOrderState());
  }

  @Test
  public void shouldNoteReadyForPickup() {
    Order order = new Order(CONSUMER_ID, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 1)));
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.noteReadyForPickup(1L);

    assertEquals(OrderState.READY_FOR_PICKUP, order.getOrderState());
  }

  @Test
  public void shouldNotePickedUp() {
    Order order = new Order(CONSUMER_ID, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 1)));
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
    Order order = new Order(CONSUMER_ID, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 1)));
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.noteDelivered(1L);

    assertEquals(OrderState.DELIVERED, order.getOrderState());
  }
}
