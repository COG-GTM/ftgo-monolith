package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private RestaurantRepository restaurantRepository;

  @Mock
  private MeterRegistry meterRegistry;

  @Mock
  private ConsumerService consumerService;

  @Mock
  private CourierRepository courierRepository;

  @Mock
  private CourierAssignmentStrategy courierAssignmentStrategy;

  @Mock
  private Counter counter;

  private OrderService orderService;

  private Restaurant restaurant;

  @BeforeEach
  void setUp() {
    orderService = new OrderService(orderRepository, restaurantRepository,
            Optional.of(meterRegistry), consumerService, courierRepository, courierAssignmentStrategy);

    MenuItem menuItem = new MenuItem("1", "Chicken", new Money("10.00"));
    restaurant = new Restaurant(1L, "Ajanta", new RestaurantMenu(Collections.singletonList(menuItem)));
  }

  @Test
  void shouldCreateOrder() {
    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    when(meterRegistry.counter(anyString())).thenReturn(counter);

    List<MenuItemIdAndQuantity> lineItems = Collections.singletonList(
            new MenuItemIdAndQuantity("1", 2));

    Order order = orderService.createOrder(100L, 1L, lineItems);

    assertThat(order).isNotNull();
    assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
    assertThat(order.getConsumerId()).isEqualTo(100L);
    verify(orderRepository).save(any(Order.class));
    verify(consumerService).validateOrderForConsumer(eq(100L), any(Money.class));
  }

  @Test
  void shouldThrowWhenRestaurantNotFound() {
    when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

    List<MenuItemIdAndQuantity> lineItems = Collections.singletonList(
            new MenuItemIdAndQuantity("1", 2));

    assertThatThrownBy(() -> orderService.createOrder(100L, 999L, lineItems))
            .isInstanceOf(RestaurantNotFoundException.class);
  }

  @Test
  void shouldThrowWhenInvalidMenuItemId() {
    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

    List<MenuItemIdAndQuantity> lineItems = Collections.singletonList(
            new MenuItemIdAndQuantity("invalid", 2));

    assertThatThrownBy(() -> orderService.createOrder(100L, 1L, lineItems))
            .isInstanceOf(InvalidMenuItemIdException.class);
  }

  @Test
  void shouldCancelOrder() {
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    Order cancelled = orderService.cancel(1L);

    assertThat(cancelled.getOrderState()).isEqualTo(OrderState.CANCELLED);
  }

  @Test
  void shouldThrowWhenCancellingNonExistentOrder() {
    when(orderRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.cancel(999L))
            .isInstanceOf(OrderNotFoundException.class);
  }

  @Test
  void shouldReviseOrder() {
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 5)));
    order.setId(1L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    Map<String, Integer> revised = new HashMap<>();
    revised.put("1", 3);
    OrderRevision revision = new OrderRevision(Optional.empty(), revised);

    Order result = orderService.reviseOrder(1L, revision);

    assertThat(result).isNotNull();
  }

  @Test
  void shouldAcceptOrderAndScheduleDelivery() {
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    Address addr = new Address("1 Main", null, "Oakland", "CA", "94612", 37.8, -122.2);
    Courier courier = new Courier(new PersonName("John", "Doe"), addr);
    when(courierRepository.findAllAvailable()).thenReturn(Collections.singletonList(courier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(courier);
    when(meterRegistry.counter(anyString())).thenReturn(counter);

    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    orderService.accept(1L, readyBy);

    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
    assertThat(order.getAssignedCourier()).isEqualTo(courier);
  }

  @Test
  void shouldNotePreparing() {
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.notePreparing(1L);

    assertThat(order.getOrderState()).isEqualTo(OrderState.PREPARING);
  }

  @Test
  void shouldNoteReadyForPickup() {
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.noteReadyForPickup(1L);

    assertThat(order.getOrderState()).isEqualTo(OrderState.READY_FOR_PICKUP);
  }

  @Test
  void shouldNotePickedUp() {
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.notePickedUp(1L);

    assertThat(order.getOrderState()).isEqualTo(OrderState.PICKED_UP);
  }

  @Test
  void shouldNoteDelivered() {
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.noteDelivered(1L);

    assertThat(order.getOrderState()).isEqualTo(OrderState.DELIVERED);
  }

  @Test
  void shouldCreateOrderWithoutMeterRegistry() {
    OrderService serviceNoMetrics = new OrderService(orderRepository, restaurantRepository,
            Optional.empty(), consumerService, courierRepository, courierAssignmentStrategy);

    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    List<MenuItemIdAndQuantity> lineItems = Collections.singletonList(
            new MenuItemIdAndQuantity("1", 2));

    Order order = serviceNoMetrics.createOrder(100L, 1L, lineItems);
    assertThat(order).isNotNull();
  }

  @Test
  void shouldScheduleDeliveryWithLocationData() {
    Address restaurantAddr = new Address("1 Main", null, "Oakland", "CA", "94612", 37.8044, -122.2712);
    Restaurant locatedRestaurant = new Restaurant("Test", restaurantAddr,
            new RestaurantMenu(Collections.singletonList(new MenuItem("1", "Chicken", new Money("10.00")))));
    Order order = new Order(100L, locatedRestaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);

    Address courierAddr = new Address("2 Oak", null, "Oakland", "CA", "94612", 37.81, -122.28);
    Courier courier = new Courier(new PersonName("Jane", "Doe"), courierAddr);
    courier.updateLocation(37.81, -122.28);
    when(courierRepository.findAllAvailable()).thenReturn(Collections.singletonList(courier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(courier);
    when(meterRegistry.counter(anyString())).thenReturn(counter);

    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    orderService.scheduleDelivery(order, readyBy);

    assertThat(order.getAssignedCourier()).isEqualTo(courier);
    assertThat(courier.getPlan().getActions()).hasSize(2);
  }

  @Test
  void shouldScheduleDeliveryWhenReadyByIsBeforePickupArrival() {
    Address restaurantAddr = new Address("1 Main", null, "San Francisco", "CA", "94102", 37.7749, -122.4194);
    Restaurant farRestaurant = new Restaurant("Far Restaurant", restaurantAddr,
            new RestaurantMenu(Collections.singletonList(new MenuItem("1", "Chicken", new Money("10.00")))));
    Order order = new Order(100L, farRestaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);

    Address courierAddr = new Address("2 Oak", null, "Oakland", "CA", "94612", 37.81, -122.28);
    Courier courier = new Courier(new PersonName("Jane", "Doe"), courierAddr);
    courier.updateLocation(37.81, -122.28);
    when(courierRepository.findAllAvailable()).thenReturn(Collections.singletonList(courier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(courier);
    when(meterRegistry.counter(anyString())).thenReturn(counter);

    // readyBy very soon - pickup arrival will be after readyBy
    LocalDateTime readyBy = LocalDateTime.now().plusMinutes(1);
    orderService.scheduleDelivery(order, readyBy);

    assertThat(order.getAssignedCourier()).isEqualTo(courier);
  }

  @Test
  void shouldScheduleDeliveryWhenRestaurantAddressIsNull() {
    Restaurant noAddrRestaurant = new Restaurant("No Addr", null,
            new RestaurantMenu(Collections.singletonList(new MenuItem("1", "Chicken", new Money("10.00")))));
    Order order = new Order(100L, noAddrRestaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);

    Courier courier = new Courier(new PersonName("Jane", "Doe"),
            new Address("2 Oak", null, "Oakland", "CA", "94612", 37.81, -122.28));
    courier.updateLocation(37.81, -122.28);
    when(courierRepository.findAllAvailable()).thenReturn(Collections.singletonList(courier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(courier);
    when(meterRegistry.counter(anyString())).thenReturn(counter);

    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    orderService.scheduleDelivery(order, readyBy);

    assertThat(order.getAssignedCourier()).isEqualTo(courier);
  }

  @Test
  void shouldScheduleDeliveryWithoutLocationData() {
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);

    Courier courier = new Courier(new PersonName("Jane", "Doe"),
            new Address("2 Oak", null, "Oakland", "CA", "94612"));
    when(courierRepository.findAllAvailable()).thenReturn(Collections.singletonList(courier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(courier);
    when(meterRegistry.counter(anyString())).thenReturn(counter);

    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    orderService.scheduleDelivery(order, readyBy);

    assertThat(order.getAssignedCourier()).isEqualTo(courier);
  }
}
