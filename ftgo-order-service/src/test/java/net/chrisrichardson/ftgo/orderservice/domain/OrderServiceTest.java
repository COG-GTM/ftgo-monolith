package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.Counter;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

  private OrderRepository orderRepository;
  private RestaurantRepository restaurantRepository;
  private MeterRegistry meterRegistry;
  private ConsumerService consumerService;
  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;
  private OrderService orderService;
  private Counter counter;

  @Before
  public void setUp() {
    orderRepository = mock(OrderRepository.class);
    restaurantRepository = mock(RestaurantRepository.class);
    meterRegistry = mock(MeterRegistry.class);
    consumerService = mock(ConsumerService.class);
    courierRepository = mock(CourierRepository.class);
    courierAssignmentStrategy = mock(CourierAssignmentStrategy.class);
    counter = mock(Counter.class);

    when(meterRegistry.counter(anyString())).thenReturn(counter);

    orderService = new OrderService(orderRepository, restaurantRepository,
            Optional.of(meterRegistry), consumerService, courierRepository, courierAssignmentStrategy);
  }

  @Test
  public void shouldCreateOrder() {
    Restaurant restaurant = new Restaurant("TestR",
            new Address("1", null, "C", "S", "Z", 40.7, -74.0),
            new RestaurantMenu(Arrays.asList(new MenuItem("m1", "Burger", new Money("10.00")))));
    restaurant.setId(1L);

    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      Order o = invocation.getArgument(0);
      o.setId(1L);
      return o;
    });

    List<MenuItemIdAndQuantity> items = Arrays.asList(new MenuItemIdAndQuantity("m1", 2));
    Order order = orderService.createOrder(100L, 1L, items);

    assertThat(order).isNotNull();
    assertThat(order.getConsumerId()).isEqualTo(100L);
    verify(consumerService).validateOrderForConsumer(eq(100L), any(Money.class));
    verify(orderRepository).save(any(Order.class));
    verify(meterRegistry, times(2)).counter(anyString());
  }

  @Test
  public void shouldThrowWhenRestaurantNotFound() {
    when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

    List<MenuItemIdAndQuantity> items = Arrays.asList(new MenuItemIdAndQuantity("m1", 1));
    assertThatThrownBy(() -> orderService.createOrder(1L, 999L, items))
            .isInstanceOf(RestaurantNotFoundException.class);
  }

  @Test
  public void shouldThrowWhenMenuItemNotFound() {
    Restaurant restaurant = new Restaurant("TestR",
            new Address("1", null, "C", "S", "Z"),
            new RestaurantMenu(Arrays.asList(new MenuItem("m1", "Burger", new Money("10.00")))));
    restaurant.setId(1L);
    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

    List<MenuItemIdAndQuantity> items = Arrays.asList(new MenuItemIdAndQuantity("nonexistent", 1));
    assertThatThrownBy(() -> orderService.createOrder(1L, 1L, items))
            .isInstanceOf(InvalidMenuItemIdException.class);
  }

  @Test
  public void shouldCancelOrder() {
    Order order = makeTestOrder();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    Order cancelled = orderService.cancel(1L);
    assertThat(cancelled.getOrderState()).isEqualTo(OrderState.CANCELLED);
  }

  @Test
  public void shouldThrowWhenCancellingNonExistentOrder() {
    when(orderRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> orderService.cancel(999L))
            .isInstanceOf(OrderNotFoundException.class);
  }

  @Test
  public void shouldReviseOrder() {
    Order order = makeTestOrder();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    OrderRevision revision = new OrderRevision(Optional.empty(),
            java.util.Collections.singletonMap("m1", 1));
    Order revised = orderService.reviseOrder(1L, revision);
    assertThat(revised).isNotNull();
  }

  @Test
  public void shouldAcceptOrder() {
    Order order = makeTestOrder();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    Courier courier = new Courier(new PersonName("J", "D"),
            new Address("1", null, "C", "S", "Z", 40.7, -74.0));
    when(courierRepository.findAllAvailable()).thenReturn(Arrays.asList(courier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(courier);

    LocalDateTime readyBy = LocalDateTime.now().plusMinutes(30);
    orderService.accept(1L, readyBy);

    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
  }

  @Test
  public void shouldNotePreparing() {
    Order order = makeTestOrder();
    order.acceptTicket(LocalDateTime.now().plusMinutes(30));
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.notePreparing(1L);
    assertThat(order.getOrderState()).isEqualTo(OrderState.PREPARING);
  }

  @Test
  public void shouldNoteReadyForPickup() {
    Order order = makeTestOrder();
    order.acceptTicket(LocalDateTime.now().plusMinutes(30));
    order.notePreparing();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.noteReadyForPickup(1L);
    assertThat(order.getOrderState()).isEqualTo(OrderState.READY_FOR_PICKUP);
  }

  @Test
  public void shouldNotePickedUp() {
    Order order = makeTestOrder();
    order.acceptTicket(LocalDateTime.now().plusMinutes(30));
    order.notePreparing();
    order.noteReadyForPickup();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.notePickedUp(1L);
    assertThat(order.getOrderState()).isEqualTo(OrderState.PICKED_UP);
  }

  @Test
  public void shouldNoteDelivered() {
    Order order = makeTestOrder();
    order.acceptTicket(LocalDateTime.now().plusMinutes(30));
    order.notePreparing();
    order.noteReadyForPickup();
    order.notePickedUp();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.noteDelivered(1L);
    assertThat(order.getOrderState()).isEqualTo(OrderState.DELIVERED);
  }

  @Test
  public void shouldAcceptOrderWithCourierWithoutLocation() {
    // Courier without coordinates - exercises fallback branch in estimateDeliveryTime
    Order order = makeTestOrder();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    Courier courier = new Courier(new PersonName("J", "D"), new Address("1", null, "C", "S", "Z"));
    when(courierRepository.findAllAvailable()).thenReturn(Arrays.asList(courier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(courier);

    LocalDateTime readyBy = LocalDateTime.now().plusMinutes(30);
    orderService.accept(1L, readyBy);
    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
  }

  @Test
  public void shouldAcceptOrderWithNullRestaurantAddress() {
    // Restaurant with null address - exercises another branch
    Restaurant restaurant = new Restaurant("TestR", null,
            new RestaurantMenu(Arrays.asList(new MenuItem("m1", "Burger", new Money("10.00")))));
    restaurant.setId(1L);
    List<OrderLineItem> items = Arrays.asList(new OrderLineItem("m1", "Burger", new Money("10.00"), 2));
    Order order = new Order(1L, restaurant, items);
    order.setId(1L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    Courier courier = new Courier(new PersonName("J", "D"),
            new Address("1", null, "C", "S", "Z", 40.7, -74.0));
    when(courierRepository.findAllAvailable()).thenReturn(Arrays.asList(courier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(courier);

    LocalDateTime readyBy = LocalDateTime.now().plusMinutes(30);
    orderService.accept(1L, readyBy);
    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
  }

  @Test
  public void shouldAcceptOrderWithNullLatitude() {
    // Restaurant address has null latitude
    Restaurant restaurant = new Restaurant("TestR",
            new Address("1", null, "C", "S", "Z"),
            new RestaurantMenu(Arrays.asList(new MenuItem("m1", "Burger", new Money("10.00")))));
    restaurant.setId(1L);
    List<OrderLineItem> items = Arrays.asList(new OrderLineItem("m1", "Burger", new Money("10.00"), 2));
    Order order = new Order(1L, restaurant, items);
    order.setId(1L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    Courier courier = new Courier(new PersonName("J", "D"),
            new Address("1", null, "C", "S", "Z", 40.7, -74.0));
    when(courierRepository.findAllAvailable()).thenReturn(Arrays.asList(courier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(courier);

    LocalDateTime readyBy = LocalDateTime.now().plusMinutes(30);
    orderService.accept(1L, readyBy);
    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
  }

  @Test
  public void shouldAcceptOrderWithFarAwayCourier() {
    // Courier far from restaurant - exercises pickupArrival.isAfter(readyBy) == true branch
    Order order = makeTestOrder();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    // Place courier far away from restaurant (restaurant is at 40.7, -74.0)
    Courier courier = new Courier(new PersonName("J", "D"),
            new Address("1", null, "C", "S", "Z", 51.5, -0.1)); // London coords
    when(courierRepository.findAllAvailable()).thenReturn(Arrays.asList(courier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(courier);

    // Set readyBy to very soon, so pickupArrival > readyBy
    LocalDateTime readyBy = LocalDateTime.now().plusMinutes(1);
    orderService.accept(1L, readyBy);
    assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);
  }

  @Test
  public void shouldCreateOrderWithoutMeterRegistry() {
    OrderService serviceNoMeter = new OrderService(orderRepository, restaurantRepository,
            Optional.empty(), consumerService, courierRepository, courierAssignmentStrategy);

    Restaurant restaurant = new Restaurant("TestR",
            new Address("1", null, "C", "S", "Z"),
            new RestaurantMenu(Arrays.asList(new MenuItem("m1", "Burger", new Money("10.00")))));
    restaurant.setId(1L);
    when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    List<MenuItemIdAndQuantity> items = Arrays.asList(new MenuItemIdAndQuantity("m1", 2));
    Order order = serviceNoMeter.createOrder(100L, 1L, items);
    assertThat(order).isNotNull();
  }

  private Order makeTestOrder() {
    Restaurant restaurant = new Restaurant("TestR",
            new Address("1", null, "C", "S", "Z", 40.7, -74.0),
            new RestaurantMenu(Arrays.asList(new MenuItem("m1", "Burger", new Money("10.00")))));
    restaurant.setId(1L);
    List<OrderLineItem> items = Arrays.asList(new OrderLineItem("m1", "Burger", new Money("10.00"), 2));
    Order order = new Order(1L, restaurant, items);
    order.setId(1L);
    return order;
  }
}
