package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.domain.Action;
import net.chrisrichardson.ftgo.domain.ActionType;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderLineItem;
import net.chrisrichardson.ftgo.domain.OrderRevision;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.RestaurantMother;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CONSUMER_ID;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerTest {

  private OrderService orderService;
  private OrderRepository orderRepository;
  private OrderController orderController;

  @Before
  public void setUp() throws Exception {
    orderService = mock(OrderService.class);
    orderRepository = mock(OrderRepository.class);
    orderController = new OrderController(orderService, orderRepository);
  }


  @Test
  public void shouldFindOrder() {

    when(orderRepository.findById(1L)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders/1").
    then().
            statusCode(200).
            body("orderId", equalTo(new Long(OrderDetailsMother.ORDER_ID).intValue())).
            body("state", equalTo(OrderDetailsMother.CHICKEN_VINDALOO_ORDER_STATE.name())).
            body("orderTotal", equalTo(CHICKEN_VINDALOO_ORDER_TOTAL.asString()))
    ;
  }

  @Test
  public void shouldFindNotOrder() {
    when(orderRepository.findById(1L)).thenReturn(Optional.empty());

    given().
            standaloneSetup(configureControllers(new OrderController(orderService, orderRepository))).
    when().
            get("/orders/1").
    then().
            statusCode(404)
    ;
  }

  @Test
  public void createOrderReturnsIdAndMapsRequestToServiceArguments() throws Exception {
    Order order = freshOrder();
    orderService = mock(OrderService.class);
    when(orderService.createOrder(eq(CONSUMER_ID), eq(AJANTA_ID), anyList())).thenReturn(order);
    orderController = new OrderController(orderService, orderRepository);
    MockMvc mockMvc = mockMvc(orderController);

    String response = mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"consumerId\":" + CONSUMER_ID + ",\"restaurantId\":" + AJANTA_ID
                    + ",\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":5}]}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

    assertThat(new ObjectMapper().readValue(response, CreateOrderResponse.class).getOrderId()).isEqualTo(99L);
    @SuppressWarnings("unchecked")
    org.mockito.ArgumentCaptor<List<net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity>> captor =
            org.mockito.ArgumentCaptor.forClass((Class) List.class);
    verify(orderService).createOrder(eq(CONSUMER_ID), eq(AJANTA_ID), captor.capture());
    assertThat(captor.getValue()).hasSize(1);
    assertThat(captor.getValue().get(0).getMenuItemId()).isEqualTo("1");
    assertThat(captor.getValue().get(0).getQuantity()).isEqualTo(5);
  }

  // Suspected bug: create-order exceptions currently escape without an HTTP error mapping.
  @Test
  public void createOrderWithUnknownRestaurantCurrentlyPropagatesException() {
    when(orderService.createOrder(eq(CONSUMER_ID), eq(AJANTA_ID), anyList()))
            .thenThrow(new RestaurantNotFoundException(AJANTA_ID));
    MockMvc mockMvc = mockMvc(orderController);

    assertThatThrownBy(() -> mockMvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"consumerId\":" + CONSUMER_ID + ",\"restaurantId\":" + AJANTA_ID
                    + ",\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":5}]}")))
            .hasCauseInstanceOf(RestaurantNotFoundException.class);
  }

  @Test
  public void getOrdersReturnsOrdersAndAnEmptyJsonArrayWhenNoneExist() throws Exception {
    Order first = freshOrder();
    first.setId(11L);
    Order second = freshOrder();
    second.setId(12L);
    when(orderRepository.findAllByConsumerId(1L)).thenReturn(Arrays.asList(first, second));
    MockMvc mockMvc = mockMvc(orderController);

    String response = mockMvc.perform(get("/orders").param("consumerId", "1"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    com.fasterxml.jackson.databind.JsonNode orders = objectMapper().readTree(response);
    assertThat(orders).hasSize(2);
    assertThat(orders.get(0).get("orderId").asLong()).isEqualTo(11L);
    assertThat(orders.get(0).get("state").asText()).isEqualTo(OrderState.APPROVED.name());
    assertThat(orders.get(1).get("orderId").asLong()).isEqualTo(12L);
    assertThat(orders.get(1).get("state").asText()).isEqualTo(OrderState.APPROVED.name());

    when(orderRepository.findAllByConsumerId(2L)).thenReturn(Collections.emptyList());
    String empty = mockMvc.perform(get("/orders").param("consumerId", "2"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    assertThat(objectMapper().readTree(empty).size()).isEqualTo(0);
  }

  @Test
  public void getOrdersRequiresConsumerId() throws Exception {
    mockMvc(orderController).perform(get("/orders")).andExpect(status().isBadRequest());
  }

  @Test
  public void getOrderIncludesAssignedCourierActionsAndEstimatedDelivery() throws Exception {
    Order order = freshOrder();
    Courier courier = new Courier();
    ReflectionTestUtils.setField(courier, "id", 7L);
    LocalDateTime deliveryTime = LocalDateTime.of(2030, 1, 1, 12, 30);
    courier.addAction(Action.makePickup(order));
    courier.addAction(Action.makeDropoff(order, deliveryTime));
    order.schedule(courier);
    when(orderRepository.findById(99L)).thenReturn(Optional.of(order));

    com.fasterxml.jackson.databind.JsonNode response = responseForOrder(99L);

    assertThat(response.get("assignedCourier").asLong()).isEqualTo(7L);
    assertThat(response.get("courierActions")).hasSize(2);
    assertThat(response.get("courierActions").get(0).get("type").asText()).isEqualTo(ActionType.PICKUP.name());
    assertThat(response.get("courierActions").get(1).get("type").asText()).isEqualTo(ActionType.DROPOFF.name());
    assertThat(response.get("estimatedDeliveryTime").asText()).isEqualTo("2030-01-01T12:30:00");
    assertThat(response.get("restaurantName").asText()).isEqualTo(RestaurantMother.AJANTA_RESTAURANT_NAME);
  }

  @Test
  public void getOrderWithPickupOnlyCourierHasNoEstimatedDeliveryTime() throws Exception {
    Order order = freshOrder();
    Courier courier = new Courier();
    ReflectionTestUtils.setField(courier, "id", 7L);
    courier.addAction(Action.makePickup(order));
    order.schedule(courier);
    when(orderRepository.findById(99L)).thenReturn(Optional.of(order));

    com.fasterxml.jackson.databind.JsonNode response = responseForOrder(99L);

    assertThat(response.get("courierActions")).hasSize(1);
    assertThat(response.get("estimatedDeliveryTime").isNull()).isTrue();
  }

  @Test
  public void getOrderWithoutCourierHasNullCourierFields() throws Exception {
    when(orderRepository.findById(99L)).thenReturn(Optional.of(freshOrder()));

    com.fasterxml.jackson.databind.JsonNode response = responseForOrder(99L);

    assertThat(response.get("assignedCourier").isNull()).isTrue();
    assertThat(response.get("courierActions").isNull()).isTrue();
    assertThat(response.get("estimatedDeliveryTime").isNull()).isTrue();
  }

  @Test
  public void cancelReturnsCancelledOrderAndMapsMissingOrderToNotFound() throws Exception {
    Order cancelled = freshOrder();
    cancelled.cancel();
    when(orderService.cancel(99L)).thenReturn(cancelled);
    MockMvc mockMvc = mockMvc(orderController);
    String response = mockMvc.perform(post("/orders/99/cancel"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    assertThat(objectMapper().readTree(response).get("state").asText()).isEqualTo(OrderState.CANCELLED.name());

    when(orderService.cancel(98L)).thenThrow(new OrderNotFoundException(98L));
    mockMvc.perform(post("/orders/98/cancel")).andExpect(status().isNotFound());
  }

  // Suspected bug: cancel currently maps not-found errors but lets state-transition errors escape.
  @Test
  public void cancelWithUnsupportedStateTransitionCurrentlyPropagatesException() {
    when(orderService.cancel(99L)).thenThrow(new UnsupportedStateTransitionException(OrderState.ACCEPTED));

    assertThatThrownBy(() -> mockMvc(orderController).perform(post("/orders/99/cancel")))
            .hasCauseInstanceOf(UnsupportedStateTransitionException.class);
  }

  @Test
  public void reviseReturnsOrderAndPassesRevisionWithoutDeliveryInformation() throws Exception {
    Order order = freshOrder();
    when(orderService.reviseOrder(eq(99L), any(OrderRevision.class))).thenReturn(order);
    String response = mockMvc(orderController).perform(post("/orders/99/revise")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"revisedLineItemQuantities\":{\"1\":3}}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

    assertThat(objectMapper().readTree(response).get("orderId").asLong()).isEqualTo(99L);
    org.mockito.ArgumentCaptor<OrderRevision> captor = org.mockito.ArgumentCaptor.forClass(OrderRevision.class);
    verify(orderService).reviseOrder(eq(99L), captor.capture());
    assertThat(captor.getValue().getDeliveryInformation()).isEmpty();
    assertThat(captor.getValue().getRevisedLineItemQuantities()).containsEntry("1", 3);

    when(orderService.reviseOrder(eq(98L), any(OrderRevision.class))).thenThrow(new OrderNotFoundException(98L));
    mockMvc(orderController).perform(post("/orders/98/revise")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"revisedLineItemQuantities\":{\"1\":3}}"))
            .andExpect(status().isNotFound());
  }

  @Test
  public void acceptPassesReadyByToService() throws Exception {
    mockMvc(orderController).perform(post("/orders/99/accept")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"readyBy\":\"2030-01-01T12:00:00\"}"))
            .andExpect(status().isOk());

    verify(orderService).accept(99L, LocalDateTime.of(2030, 1, 1, 12, 0));
  }

  // Suspected bug: accept currently propagates OrderNotFoundException instead of returning 404.
  @Test
  public void acceptWithMissingOrderCurrentlyPropagatesException() {
    doThrow(new OrderNotFoundException(99L)).when(orderService).accept(99L, LocalDateTime.of(2030, 1, 1, 12, 0));

    assertThatThrownBy(() -> mockMvc(orderController).perform(post("/orders/99/accept")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"readyBy\":\"2030-01-01T12:00:00\"}")))
            .hasCauseInstanceOf(OrderNotFoundException.class);
  }

  @Test
  public void lifecycleEndpointsCallTheirMatchingServiceMethods() throws Exception {
    MockMvc mockMvc = mockMvc(orderController);
    mockMvc.perform(post("/orders/99/preparing")).andExpect(status().isOk());
    mockMvc.perform(post("/orders/99/ready")).andExpect(status().isOk());
    mockMvc.perform(post("/orders/99/pickedup")).andExpect(status().isOk());
    mockMvc.perform(post("/orders/99/delivered")).andExpect(status().isOk());

    verify(orderService).notePreparing(99L);
    verify(orderService).noteReadyForPickup(99L);
    verify(orderService).notePickedUp(99L);
    verify(orderService).noteDelivered(99L);
  }

  // Suspected bug: lifecycle endpoints currently propagate OrderNotFoundException instead of returning 404.
  @Test
  public void preparingWithMissingOrderCurrentlyPropagatesException() {
    doThrow(new OrderNotFoundException(99L)).when(orderService).notePreparing(99L);

    assertThatThrownBy(() -> mockMvc(orderController).perform(post("/orders/99/preparing")))
            .hasCauseInstanceOf(OrderNotFoundException.class);
  }

  private com.fasterxml.jackson.databind.JsonNode responseForOrder(long orderId) throws Exception {
    String body = mockMvc(orderController).perform(get("/orders/" + orderId))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    return objectMapper().readTree(body);
  }

  private MockMvc mockMvc(Object... controllers) {
    return configureControllers(controllers).build();
  }

  private ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new MoneyModule());
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }

  private Order freshOrder() {
    Order order = new Order(CONSUMER_ID, RestaurantMother.AJANTA_RESTAURANT,
            Collections.singletonList(new OrderLineItem("1", "Chicken Vindaloo", new Money("12.34"), 5)));
    order.setId(99L);
    return order;
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper());
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }

}