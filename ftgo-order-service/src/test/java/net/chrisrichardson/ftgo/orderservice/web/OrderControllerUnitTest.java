package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class OrderControllerUnitTest {

  private OrderService orderService;
  private OrderRepository orderRepository;
  private OrderController controller;

  @Before
  public void setUp() {
    orderService = mock(OrderService.class);
    orderRepository = mock(OrderRepository.class);
    controller = new OrderController(orderService, orderRepository);
  }

  @Test
  public void shouldGetOrderReturns200() {
    Order order = makeTestOrder();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    ResponseEntity<GetOrderResponse> response = controller.getOrder(1L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getOrderId()).isEqualTo(1L);
    assertThat(response.getBody().getState()).isEqualTo("APPROVED");
  }

  @Test
  public void shouldGetOrderReturns404WhenNotFound() {
    when(orderRepository.findById(999L)).thenReturn(Optional.empty());

    ResponseEntity<GetOrderResponse> response = controller.getOrder(999L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldGetOrdersByConsumerId() {
    Order order = makeTestOrder();
    when(orderRepository.findAllByConsumerId(1L)).thenReturn(Arrays.asList(order));

    ResponseEntity<List<GetOrderResponse>> response = controller.getOrders(1L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
  }

  @Test
  public void shouldCancelOrder() {
    Order order = makeTestOrder();
    when(orderService.cancel(1L)).thenReturn(order);

    ResponseEntity<GetOrderResponse> response = controller.cancel(1L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldReturn404WhenCancellingNonExistent() {
    when(orderService.cancel(999L)).thenThrow(new OrderNotFoundException(999L));

    ResponseEntity<GetOrderResponse> response = controller.cancel(999L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReviseOrder() {
    Order order = makeTestOrder();
    when(orderService.reviseOrder(anyLong(), any(OrderRevision.class))).thenReturn(order);

    net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest request =
            new net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest(Collections.singletonMap("m1", 3));

    ResponseEntity<GetOrderResponse> response = controller.revise(1L, request);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldReturn404WhenRevisingNonExistent() {
    when(orderService.reviseOrder(anyLong(), any(OrderRevision.class)))
            .thenThrow(new OrderNotFoundException(999L));

    net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest request =
            new net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest(Collections.singletonMap("m1", 3));

    ResponseEntity<GetOrderResponse> response = controller.revise(999L, request);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldCreateOrder() {
    Order order = makeTestOrder();
    when(orderService.createOrder(anyLong(), anyLong(), anyList())).thenReturn(order);

    net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest request =
            new net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest(
                    1L, 1L,
                    Arrays.asList(new net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest.LineItem("m1", 2))
            );

    net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse response = controller.create(request);
    assertThat(response.getOrderId()).isEqualTo(1L);
  }

  @Test
  public void shouldAcceptOrder() {
    LocalDateTime readyBy = LocalDateTime.now().plusMinutes(30);
    net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance acceptance =
            new net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance(readyBy);

    ResponseEntity<String> response = controller.accept(1L, acceptance);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(orderService).accept(1L, readyBy);
  }

  @Test
  public void shouldNotePreparing() {
    ResponseEntity<String> response = controller.preparing(1L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(orderService).notePreparing(1L);
  }

  @Test
  public void shouldNoteReady() {
    ResponseEntity<String> response = controller.ready(1L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(orderService).noteReadyForPickup(1L);
  }

  @Test
  public void shouldNotePickedUp() {
    ResponseEntity<String> response = controller.pickedup(1L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(orderService).notePickedUp(1L);
  }

  @Test
  public void shouldNoteDelivered() {
    ResponseEntity<String> response = controller.delivered(1L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(orderService).noteDelivered(1L);
  }

  @Test
  public void shouldGetOrderWithAssignedCourier() {
    Order order = makeTestOrder();
    net.chrisrichardson.ftgo.common.PersonName courierName = new net.chrisrichardson.ftgo.common.PersonName("John", "Doe");
    Courier courier = new Courier(courierName, new Address("1", null, "C", "S", "Z", 40.0, -74.0));
    courier.noteAvailable();
    Action dropoff = Action.makeDropoff(order, LocalDateTime.now().plusMinutes(30));
    courier.addAction(dropoff);
    order.schedule(courier);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    ResponseEntity<GetOrderResponse> response = controller.getOrder(1L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    // Courier has no persisted ID so assignedCourier is null in response
    // but courierActions is populated from courier.actionsForDelivery(order)
    assertThat(response.getBody().getCourierActions()).isNotEmpty();
    assertThat(response.getBody().getEstimatedDeliveryTime()).isNotNull();
  }

  private Order makeTestOrder() {
    Restaurant restaurant = new Restaurant("TestR",
            new Address("1", null, "C", "S", "Z"),
            new RestaurantMenu(Arrays.asList(new MenuItem("m1", "Burger", new Money("10.00")))));
    restaurant.setId(1L);
    List<OrderLineItem> items = Arrays.asList(new OrderLineItem("m1", "Burger", new Money("10.00"), 2));
    Order order = new Order(1L, restaurant, items);
    order.setId(1L);
    return order;
  }
}
