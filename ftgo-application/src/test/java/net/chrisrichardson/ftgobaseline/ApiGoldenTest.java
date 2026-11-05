package net.chrisrichardson.ftgobaseline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.chrisrichardson.ftgo.GlobalExceptionHandler;
import net.chrisrichardson.ftgo.SentryConfigController;
import net.chrisrichardson.ftgo.WebCompatibilityConfiguration;
import net.chrisrichardson.ftgo.common.*;
import net.chrisrichardson.ftgo.common.tracking.*;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.consumerservice.web.ConsumerController;
import net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException;
import net.chrisrichardson.ftgo.courierservice.domain.CourierService;
import net.chrisrichardson.ftgo.courierservice.web.CourierController;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException;
import net.chrisrichardson.ftgo.orderservice.web.OrderController;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.web.RestaurantController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@TestPropertySource(properties = {"sentry.dsn=", "sentry.environment=demo", "sentry.send-default-pii=true"})
@ContextConfiguration(classes = ApiGoldenTest.Config.class)
public class ApiGoldenTest {

  @Configuration
  @Import({ConsumerController.class, RestaurantController.class, OrderController.class,
          CourierController.class, ApiTrackingController.class, SentryConfigController.class,
          GlobalExceptionHandler.class, CommonJsonMapperInitializer.class, WebCompatibilityConfiguration.class})
  static class Config {
    @Bean
    ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
  }

  private static final LocalDateTime TIME = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
  private static final PersonName NAME = new PersonName("Ada", "Lovelace");
  private static final Address ADDRESS = new Address("1 Main St", null, "Oakland", "CA", "94619", 37.8, -122.2);

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper mapper;
  @MockitoBean private ConsumerService consumers;
  @MockitoBean private RestaurantService restaurants;
  @MockitoBean private OrderService orders;
  @MockitoBean private OrderRepository orderRepository;
  @MockitoBean private CourierService couriers;
  @MockitoBean private ApiRequestLogRepository logs;

  private Order order;
  private Courier courier;
  private ApiRequestLog log;

  @BeforeEach
  public void setUp() {
    Consumer consumer = new Consumer(NAME);
    ReflectionTestUtils.setField(consumer, "id", 1L);
    when(consumers.create(any(PersonName.class))).thenReturn(consumer);
    when(consumers.findById(1L)).thenReturn(Optional.of(consumer));

    Restaurant restaurant = new Restaurant(2L, "Golden Kitchen", new RestaurantMenu(Collections.emptyList()));
    when(restaurants.create(any(CreateRestaurantRequest.class))).thenReturn(restaurant);
    when(restaurants.findById(2L)).thenReturn(Optional.of(restaurant));

    order = mock(Order.class);
    when(order.getId()).thenReturn(3L);
    when(order.getOrderState()).thenReturn(OrderState.APPROVED);
    when(order.getOrderTotal()).thenReturn(new Money("24.68"));
    when(order.getRestaurant()).thenReturn(restaurant);
    when(orders.createOrder(eq(1L), eq(2L), anyList())).thenReturn(order);
    when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
    when(orderRepository.findAllByConsumerId(1L)).thenReturn(Collections.singletonList(order));
    when(orders.cancel(3L)).thenReturn(order);
    when(orders.reviseOrder(eq(3L), any(OrderRevision.class))).thenReturn(order);

    courier = new Courier(NAME, ADDRESS);
    ReflectionTestUtils.setField(courier, "id", 4L);
    ReflectionTestUtils.setField(courier, "lastLocationUpdate", TIME);
    courier.noteAvailable();
    courier.addAction(new Action(ActionType.PICKUP, order, TIME));
    courier.addAction(new Action(ActionType.DROPOFF, order, TIME.plusMinutes(20)));
    when(couriers.createCourier(any(PersonName.class), any(Address.class))).thenReturn(courier);
    when(couriers.findCourierById(4L)).thenReturn(courier);

    log = new ApiRequestLog("golden-correlation", "GET", "/orders/3", "consumerId=1", "127.0.0.1", "golden-client");
    ReflectionTestUtils.setField(log, "id", 5L);
    ReflectionTestUtils.setField(log, "requestTimestamp", TIME);
    log.complete(500, 25L, "example failure");
    when(logs.findRecentLogs(any(LocalDateTime.class))).thenReturn(Collections.singletonList(log));
    when(logs.findErrorsSince(any(LocalDateTime.class))).thenReturn(Collections.singletonList(log));
    when(logs.findByRequestUri("/orders/3")).thenReturn(Collections.singletonList(log));
    when(logs.findByCorrelationId("golden-correlation")).thenReturn(log);
  }

  @Test
  public void consumerCreate() throws Exception {
    golden(post("/consumers").contentType(MediaType.APPLICATION_JSON).content(fixture("consumer-request")), "consumer-created");
    verify(consumers).create(refEq(NAME));
  }

  @Test
  public void consumerGet() throws Exception {
    golden(get("/consumers/1"), "consumer");
  }

  @Test
  public void restaurantCreate() throws Exception {
    golden(post("/restaurants").contentType(MediaType.APPLICATION_JSON).content(fixture("restaurant-request")), "restaurant-created");
    verify(restaurants).create(argThat(r -> r.getMenu().getMenuItemDTOs().get(0).getPrice().equals(new Money("12.34"))));
  }

  @Test
  public void restaurantGet() throws Exception {
    golden(get("/restaurants/2"), "restaurant");
  }

  @Test
  public void orderCreate() throws Exception {
    golden(post("/orders").contentType(MediaType.APPLICATION_JSON).content(fixture("order-request")), "order-created");
    verify(orders).createOrder(eq(1L), eq(2L), argThat(items -> items.size() == 1 && items.get(0).getQuantity() == 2));
  }

  @Test
  public void orderGetUnassigned() throws Exception {
    golden(get("/orders/3"), "order");
  }

  @Test
  public void orderGetAssignedUsesIsoTimeAndStringMoney() throws Exception {
    when(order.getAssignedCourier()).thenReturn(courier);
    golden(get("/orders/3"), "order-assigned");
  }

  @Test
  public void orderHistoryAndTrailingSlash() throws Exception {
    golden(get("/orders").param("consumerId", "1"), "order-history");
    golden(get("/orders/").param("consumerId", "1"), "order-history");
    golden(get("/orders/3/"), "order");
    golden(get("/consumers/1/"), "consumer");
    golden(post("/orders/").contentType(MediaType.APPLICATION_JSON).content(fixture("order-request")), "order-created");
  }

  @Test
  public void springMvcExceptionsAreClientErrors() throws Exception {
    springMvcError(get("/orders"), 400, "Bad Request");
    springMvcError(get("/orders/not-a-number"), 400, "Bad Request");
    springMvcError(post("/consumers").contentType(MediaType.APPLICATION_JSON).content("{not json"), 400, "Bad Request");
    springMvcError(delete("/orders/3"), 405, "Method Not Allowed");
    springMvcError(post("/consumers").contentType(MediaType.TEXT_PLAIN).content("x"), 415, "Unsupported Media Type");
  }

  @Test
  public void errorPathKeepsTrailingSlash() throws Exception {
    MvcResult result = mvc.perform(get("/orders/not-a-number/")).andExpect(status().isBadRequest()).andReturn();
    assertThat(mapper.readTree(result.getResponse().getContentAsString()).get("path").asText())
            .isEqualTo("/orders/not-a-number/");
  }

  private void springMvcError(MockHttpServletRequestBuilder request, int status, String error) throws Exception {
    MvcResult result = mvc.perform(request).andExpect(status().is(status))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn();
    JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
    assertThat(body.get("status").asInt()).isEqualTo(status);
    assertThat(body.get("error").asText()).isEqualTo(error);
    assertThat(body.get("message").asText()).isNotEmpty();
    assertThat(body.has("path")).isTrue();
    assertThat(body.has("timestamp")).isTrue();
  }

  @Test
  public void orderCancel() throws Exception {
    when(order.getOrderState()).thenReturn(OrderState.CANCELLED);
    golden(post("/orders/3/cancel"), "order-cancelled");
    verify(orders).cancel(3L);
  }

  @Test
  public void orderRevise() throws Exception {
    when(order.getOrderTotal()).thenReturn(new Money("37.02"));
    golden(post("/orders/3/revise").contentType(MediaType.APPLICATION_JSON).content("{\"revisedLineItemQuantities\":{\"1\":3}}"), "order-revised");
    verify(orders).reviseOrder(eq(3L), argThat(r -> r.getRevisedLineItemQuantities().get("1") == 3));
  }

  @Test
  public void orderAcceptParsesIsoTime() throws Exception {
    empty(post("/orders/3/accept").contentType(MediaType.APPLICATION_JSON).content("{\"readyBy\":\"2026-01-02T03:04:05\"}"));
    verify(orders).accept(3L, TIME);
  }

  @Test
  public void orderPreparing() throws Exception {
    empty(post("/orders/3/preparing"));
    verify(orders).notePreparing(3L);
  }

  @Test
  public void orderReady() throws Exception {
    empty(post("/orders/3/ready"));
    verify(orders).noteReadyForPickup(3L);
  }

  @Test
  public void orderPickedUp() throws Exception {
    empty(post("/orders/3/pickedup"));
    verify(orders).notePickedUp(3L);
  }

  @Test
  public void orderDelivered() throws Exception {
    empty(post("/orders/3/delivered"));
    verify(orders).noteDelivered(3L);
  }

  @Test
  public void courierCreate() throws Exception {
    golden(post("/couriers").contentType(MediaType.APPLICATION_JSON).content(fixture("courier-request")), "courier-created");
    verify(couriers).createCourier(refEq(NAME), refEq(ADDRESS));
  }

  @Test
  public void courierGet() throws Exception {
    golden(get("/couriers/4"), "courier");
  }

  @Test
  public void courierAvailability() throws Exception {
    empty(post("/couriers/4/availability").contentType(MediaType.APPLICATION_JSON).content("{\"available\":false}"));
    verify(couriers).updateAvailability(4L, false);
  }

  @Test
  public void courierLocation() throws Exception {
    empty(post("/couriers/4/location").contentType(MediaType.APPLICATION_JSON).content("{\"latitude\":37.8,\"longitude\":-122.2}"));
    verify(couriers).updateLocation(4L, 37.8, -122.2);
  }

  @Test
  public void courierWorkload() throws Exception {
    golden(get("/couriers/4/workload"), "courier-workload");
  }

  @Test
  public void trackingRecent() throws Exception {
    golden(get("/api/tracking/logs"), "tracking-logs");
    verify(logs).findRecentLogs(any(LocalDateTime.class));
  }

  @Test
  public void trackingErrors() throws Exception {
    golden(get("/api/tracking/logs/errors").param("minutesBack", "10"), "tracking-logs");
    verify(logs).findErrorsSince(any(LocalDateTime.class));
  }

  @Test
  public void trackingSearch() throws Exception {
    golden(get("/api/tracking/logs/search").param("uri", "/orders/3"), "tracking-logs");
    verify(logs).findByRequestUri("/orders/3");
  }

  @Test
  public void trackingCorrelation() throws Exception {
    golden(get("/api/tracking/logs/golden-correlation"), "tracking-log");
  }

  @Test
  public void trackingStats() throws Exception {
    golden(get("/api/tracking/stats").param("minutesBack", "10"), "tracking-stats");
  }

  @Test
  public void trackingEmptyStats() throws Exception {
    when(logs.findRecentLogs(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
    golden(get("/api/tracking/stats"), "tracking-empty-stats");
  }

  @Test
  public void sentryConfig() throws Exception {
    golden(get("/config/sentry"), "sentry");
  }

  @Test
  public void legacyNotFoundResponsesHaveNoBody() throws Exception {
    when(orders.cancel(999L)).thenThrow(new OrderNotFoundException(999L));
    when(orders.reviseOrder(eq(999L), any(OrderRevision.class))).thenThrow(new OrderNotFoundException(999L));
    for (String path : Arrays.asList("/consumers/999", "/restaurants/999", "/orders/999", "/api/tracking/logs/missing")) {
      mvc.perform(get(path)).andExpect(status().isNotFound()).andExpect(content().string(""));
    }
    mvc.perform(post("/orders/999/cancel")).andExpect(status().isNotFound()).andExpect(content().string(""));
    mvc.perform(post("/orders/999/revise").contentType(MediaType.APPLICATION_JSON).content("{\"revisedLineItemQuantities\":{}}"))
            .andExpect(status().isNotFound()).andExpect(content().string(""));
  }

  @Test
  public void error404Order() throws Exception {
    doThrow(new OrderNotFoundException(3L)).when(orders).notePreparing(3L);
    error(post("/orders/3/preparing"), 404, "error-order");
  }

  @Test
  public void error404Restaurant() throws Exception {
    when(orders.createOrder(anyLong(), anyLong(), anyList())).thenThrow(new RestaurantNotFoundException(2L));
    error(post("/orders").contentType(MediaType.APPLICATION_JSON).content(fixture("order-request")), 404, "error-restaurant");
  }

  @Test
  public void error404Courier() throws Exception {
    when(couriers.findCourierById(4L)).thenThrow(new CourierNotFoundException(4L));
    error(get("/couriers/4"), 404, "error-courier");
  }

  @Test
  public void error409() throws Exception {
    doThrow(new UnsupportedStateTransitionException(OrderState.APPROVED)).when(orders).notePreparing(3L);
    error(post("/orders/3/preparing"), 409, "error-conflict");
  }

  @Test
  public void error503() throws Exception {
    doThrow(new NoCourierAvailableException()).when(orders).noteReadyForPickup(3L);
    error(post("/orders/3/ready"), 503, "error-unavailable");
  }

  @Test
  public void error400() throws Exception {
    doThrow(new IllegalArgumentException("invalid location")).when(couriers).updateLocation(4L, 37.8, -122.2);
    error(post("/couriers/4/location").contentType(MediaType.APPLICATION_JSON).content("{\"latitude\":37.8,\"longitude\":-122.2}"), 400, "error-bad-request");
  }

  @Test
  public void error500DoesNotExposeException() throws Exception {
    when(couriers.findCourierById(4L)).thenThrow(new IllegalStateException("private database details"));
    error(get("/couriers/4"), 500, "error-internal");
  }

  private void golden(MockHttpServletRequestBuilder request, String name) throws Exception {
    MvcResult result = mvc.perform(request).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn();
    assertThat(mapper.readTree(result.getResponse().getContentAsString())).isEqualTo(mapper.readTree(fixture(name)));
  }

  private void empty(MockHttpServletRequestBuilder request) throws Exception {
    mvc.perform(request).andExpect(status().isOk()).andExpect(content().string(""));
  }

  private void error(MockHttpServletRequestBuilder request, int status, String name) throws Exception {
    MDC.put("correlationId", "golden-correlation");
    try {
      MvcResult result = mvc.perform(request).andExpect(status().is(status))
              .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andReturn();
      ObjectNode actual = (ObjectNode) mapper.readTree(result.getResponse().getContentAsString());
      JsonNode timestamp = actual.get("timestamp");
      assertThat(timestamp.isTextual()).isTrue();
      LocalDateTime.parse(timestamp.asText());
      actual.put("timestamp", TIME.toString());
      assertThat(actual).isEqualTo(mapper.readTree(fixture(name)));
    } finally {
      MDC.remove("correlationId");
    }
  }

  private String fixture(String name) throws Exception {
    try (java.io.InputStream input = new ClassPathResource("golden/" + name + ".json").getInputStream()) {
      return StreamUtils.copyToString(input, StandardCharsets.UTF_8);
    }
  }
}
