package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerUnitTest {

  @Mock
  private OrderService orderService;

  @Mock
  private OrderRepository orderRepository;

  private OrderController controller;
  private Restaurant restaurant;

  @BeforeEach
  void setUp() {
    controller = new OrderController(orderService, orderRepository);
    restaurant = new Restaurant(1L, "Ajanta",
            new RestaurantMenu(Collections.singletonList(
                    new MenuItem("1", "Chicken", new Money("10.00")))));
    restaurant.setId(1L);
  }

  @Test
  void shouldCreateOrder() {
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);
    when(orderService.createOrder(anyLong(), anyLong(), anyList())).thenReturn(order);

    CreateOrderRequest request = new CreateOrderRequest(100L, 1L,
            Collections.singletonList(new CreateOrderRequest.LineItem("1", 2)));

    CreateOrderResponse response = controller.create(request);

    assertThat(response.getOrderId()).isEqualTo(1L);
  }

  @Test
  void shouldGetOrder() {
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    ResponseEntity<GetOrderResponse> response = controller.getOrder(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getOrderId()).isEqualTo(1L);
    assertThat(response.getBody().getRestaurantName()).isEqualTo("Ajanta");
  }

  @Test
  void shouldReturn404WhenOrderNotFound() {
    when(orderRepository.findById(999L)).thenReturn(Optional.empty());

    ResponseEntity<GetOrderResponse> response = controller.getOrder(999L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldGetOrdersByConsumer() {
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);
    when(orderRepository.findAllByConsumerId(100L)).thenReturn(Collections.singletonList(order));

    ResponseEntity<List<GetOrderResponse>> response = controller.getOrders(100L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
  }

  @Test
  void shouldCancelOrder() {
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);
    when(orderService.cancel(1L)).thenReturn(order);

    ResponseEntity<GetOrderResponse> response = controller.cancel(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldReturn404WhenCancellingNonExistentOrder() {
    when(orderService.cancel(999L)).thenThrow(new OrderNotFoundException(999L));

    ResponseEntity<GetOrderResponse> response = controller.cancel(999L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldReviseOrder() {
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 5)));
    order.setId(1L);
    when(orderService.reviseOrder(anyLong(), any(OrderRevision.class))).thenReturn(order);

    Map<String, Integer> quantities = new HashMap<>();
    quantities.put("1", 3);
    ReviseOrderRequest request = new ReviseOrderRequest(quantities);

    ResponseEntity<GetOrderResponse> response = controller.revise(1L, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldReturn404WhenRevisingNonExistentOrder() {
    when(orderService.reviseOrder(anyLong(), any(OrderRevision.class))).thenThrow(new OrderNotFoundException(999L));

    ReviseOrderRequest request = new ReviseOrderRequest(new HashMap<>());

    ResponseEntity<GetOrderResponse> response = controller.revise(999L, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldAcceptOrder() {
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);
    OrderAcceptance acceptance = new OrderAcceptance(readyBy);

    ResponseEntity<String> response = controller.accept(1L, acceptance);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(orderService).accept(1L, readyBy);
  }

  @Test
  void shouldNotePreparing() {
    ResponseEntity<String> response = controller.preparing(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(orderService).notePreparing(1L);
  }

  @Test
  void shouldNoteReady() {
    ResponseEntity<String> response = controller.ready(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(orderService).noteReadyForPickup(1L);
  }

  @Test
  void shouldNotePickedUp() {
    ResponseEntity<String> response = controller.pickedup(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(orderService).notePickedUp(1L);
  }

  @Test
  void shouldNoteDelivered() {
    ResponseEntity<String> response = controller.delivered(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(orderService).noteDelivered(1L);
  }

  @Test
  void shouldGetOrderWithAssignedCourier() {
    Order order = new Order(100L, restaurant, Collections.singletonList(
            new OrderLineItem("1", "Chicken", new Money("10.00"), 2)));
    order.setId(1L);
    order.acceptTicket(LocalDateTime.now().plusHours(1));

    Courier courier = new Courier(new PersonName("John", "Doe"),
            new Address("1 Main", null, "Oakland", "CA", "94612", 37.8, -122.2));
    courier.addAction(Action.makePickup(order));
    courier.addAction(Action.makeDropoff(order, LocalDateTime.now().plusMinutes(30)));
    order.schedule(courier);

    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    ResponseEntity<GetOrderResponse> response = controller.getOrder(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getCourierActions()).isNotNull();
    assertThat(response.getBody().getEstimatedDeliveryTime()).isNotNull();
  }
}
