package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.security.AuthTokenService;
import net.chrisrichardson.ftgo.common.security.AuthenticatedPrincipal;
import net.chrisrichardson.ftgo.common.security.AuthenticatedPrincipalArgumentResolver;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class OrderControllerTest {

  private static final long CONSUMER_ID = 101L;
  private static final long OTHER_CONSUMER_ID = 202L;
  private static final long RESTAURANT_ID = 303L;
  private static final long OTHER_RESTAURANT_ID = 404L;
  private static final long COURIER_ID = 505L;
  private static final String MENU_ITEM_ID = "1";

  private OrderRepository orderRepository;
  private RestaurantRepository restaurantRepository;
  private ConsumerService consumerService;
  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;
  private AuthTokenService tokenService;
  private MockMvc mockMvc;
  private Order order;
  private Courier courier;
  private Order lastSavedOrder;

  @Before
  public void setUp() throws Exception {
    orderRepository = mock(OrderRepository.class);
    restaurantRepository = mock(RestaurantRepository.class);
    consumerService = mock(ConsumerService.class);
    courierRepository = mock(CourierRepository.class);
    courierAssignmentStrategy = mock(CourierAssignmentStrategy.class);
    OrderService orderService = new OrderService(orderRepository, restaurantRepository, Optional.empty(),
            consumerService, courierRepository, courierAssignmentStrategy);
    tokenService = new AuthTokenService(
            "01234567890123456789012345678901".getBytes("UTF-8"));
    Restaurant restaurant = new Restaurant(RESTAURANT_ID, "Restaurant",
            new RestaurantMenu(Collections.singletonList(
                    new MenuItem(MENU_ITEM_ID, "Item", new Money("12.34")))));
    order = new Order(CONSUMER_ID, restaurant, Collections.singletonList(
            new OrderLineItem(MENU_ITEM_ID, "Item", new Money("12.34"), 5)));
    order.setId(99L);
    courier = mock(Courier.class);
    when(courier.getId()).thenReturn(COURIER_ID);
    when(courier.hasLocation()).thenReturn(false);
    when(courier.getActiveDeliveryCount()).thenReturn(1);
    when(orderRepository.findById(anyLong())).thenAnswer(invocation -> Optional.of(order));
    when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));
    when(courierRepository.findAllAvailable()).thenReturn(Collections.singletonList(courier));
    when(courierAssignmentStrategy.assignCourier(anyList(), any(Order.class))).thenReturn(courier);
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      Order savedOrder = invocation.getArgument(0);
      savedOrder.setId(100L);
      lastSavedOrder = savedOrder;
      return savedOrder;
    });
    mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService, orderRepository))
            .setCustomArgumentResolvers(new AuthenticatedPrincipalArgumentResolver(tokenService))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper()))
            .build();
  }

  @Test
  public void shouldFindOrder() throws Exception {
    String response = mockMvc.perform(get("/orders/99")).andReturn().getResponse().getContentAsString();
    JsonNode orderResponse = objectMapper().readTree(response);
    assertEquals(99L, orderResponse.get("orderId").asLong());
    assertEquals("APPROVED", orderResponse.get("state").asText());
    assertEquals("61.70", orderResponse.get("orderTotal").asText());
  }

  @Test
  public void shouldFindNotOrder() throws Exception {
    when(orderRepository.findById(99L)).thenReturn(Optional.empty());
    assertEquals(404, mockMvc.perform(get("/orders/99")).andReturn().getResponse().getStatus());
  }

  @Test
  public void shouldRequireAuthenticationForEveryWriteEndpoint() throws Exception {
    for (String path : Arrays.asList("/orders", "/orders/99/cancel", "/orders/99/revise",
            "/orders/99/accept", "/orders/99/preparing", "/orders/99/ready",
            "/orders/99/pickedup", "/orders/99/delivered")) {
      assertEquals(path, 401, post(path, null, bodyFor(path)));
      assertEquals(path, 401, post(path, "garbage.token", bodyFor(path)));
    }
    assertEquals(OrderState.APPROVED, order.getOrderState());
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  public void shouldAuthorizeConsumerCancellationAndRevision() throws Exception {
    assertEquals(403, post("/orders/99/cancel", token(AuthenticatedPrincipal.consumer(OTHER_CONSUMER_ID)), "{}"));
    assertEquals(403, post("/orders/99/revise", token(AuthenticatedPrincipal.consumer(OTHER_CONSUMER_ID)),
            "{\"revisedLineItemQuantities\":{\"1\":4}}"));
    assertEquals(OrderState.APPROVED, order.getOrderState());

    assertEquals(200, post("/orders/99/cancel", token(AuthenticatedPrincipal.consumer(CONSUMER_ID)), "{}"));
    assertEquals(OrderState.CANCELLED, order.getOrderState());
  }

  @Test
  public void shouldAuthorizeOrderCreationAndDefaultConsumerId() throws Exception {
    String otherConsumer = createOrderBody(OTHER_CONSUMER_ID);
    assertEquals(403, post("/orders", token(AuthenticatedPrincipal.consumer(CONSUMER_ID)), otherConsumer));
    assertEquals(403, post("/orders", token(AuthenticatedPrincipal.restaurant(RESTAURANT_ID)),
            createOrderBody(CONSUMER_ID)));
    verify(orderRepository, never()).save(any(Order.class));

    assertEquals(200, post("/orders", token(AuthenticatedPrincipal.consumer(CONSUMER_ID)),
            createOrderBody(CONSUMER_ID)));
    assertEquals(CONSUMER_ID, lastSavedOrder.getConsumerId().longValue());

    lastSavedOrder = null;
    assertEquals(200, post("/orders", token(AuthenticatedPrincipal.consumer(CONSUMER_ID)),
            createOrderBody(0)));
    assertEquals(CONSUMER_ID, lastSavedOrder.getConsumerId().longValue());
  }

  @Test
  public void shouldAuthorizeRestaurantAcceptanceBeforeSchedulingCourier() throws Exception {
    assertEquals(403, post("/orders/99/accept", token(AuthenticatedPrincipal.restaurant(OTHER_RESTAURANT_ID)),
            "{\"readyBy\":\"2099-01-01T00:00:00\"}"));
    verify(courierRepository, never()).findAllAvailable();
    assertEquals(403, post("/orders/99/accept", token(AuthenticatedPrincipal.consumer(CONSUMER_ID)),
            "{\"readyBy\":\"2099-01-01T00:00:00\"}"));
    verify(courierRepository, never()).findAllAvailable();

    assertEquals(200, post("/orders/99/accept", token(AuthenticatedPrincipal.restaurant(RESTAURANT_ID)),
            "{\"readyBy\":\"2099-01-01T00:00:00\"}"));
    verify(courierRepository).findAllAvailable();
  }

  @Test
  public void shouldRequireOwningRestaurantForPreparingAndReady() throws Exception {
    assertEquals(403, post("/orders/99/preparing",
            token(AuthenticatedPrincipal.restaurant(OTHER_RESTAURANT_ID)), "{}"));
    assertEquals(403, post("/orders/99/ready",
            token(AuthenticatedPrincipal.restaurant(OTHER_RESTAURANT_ID)), "{}"));
    assertEquals(OrderState.APPROVED, order.getOrderState());
  }

  @Test
  public void shouldRequireAssignedCourierForPickupAndDelivery() throws Exception {
    assertEquals(403, post("/orders/99/pickedup", token(AuthenticatedPrincipal.courier(COURIER_ID)), "{}"));
    assertEquals(403, post("/orders/99/delivered", token(AuthenticatedPrincipal.courier(COURIER_ID)), "{}"));

    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.notePreparing();
    order.noteReadyForPickup();
    order.schedule(courier);
    assertEquals(403, post("/orders/99/pickedup",
            token(AuthenticatedPrincipal.courier(COURIER_ID + 1)), "{}"));
    assertEquals(403, post("/orders/99/delivered",
            token(AuthenticatedPrincipal.courier(COURIER_ID + 1)), "{}"));
    assertEquals(OrderState.READY_FOR_PICKUP, order.getOrderState());

    assertEquals(200, post("/orders/99/pickedup", token(AuthenticatedPrincipal.courier(COURIER_ID)), "{}"));
    assertEquals(200, post("/orders/99/delivered", token(AuthenticatedPrincipal.courier(COURIER_ID)), "{}"));
    assertEquals(OrderState.DELIVERED, order.getOrderState());
  }

  private int post(String path, String bearerToken, String body) throws Exception {
    MockHttpServletRequestBuilder request = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body);
    if (bearerToken != null) {
      request.header("Authorization", "Bearer " + bearerToken);
    }
    return mockMvc.perform(request).andReturn().getResponse().getStatus();
  }

  private String token(AuthenticatedPrincipal principal) {
    return tokenService.issue(principal, Duration.ofHours(1));
  }

  private String createOrderBody(long consumerId) throws Exception {
    return objectMapper().writeValueAsString(new CreateOrderRequest(consumerId, RESTAURANT_ID,
            Collections.singletonList(new CreateOrderRequest.LineItem(MENU_ITEM_ID, 1))));
  }

  private String bodyFor(String path) {
    if ("/orders".equals(path)) {
      try {
        return createOrderBody(CONSUMER_ID);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    if (path.endsWith("/accept")) {
      return "{\"readyBy\":\"2099-01-01T00:00:00\"}";
    }
    if (path.endsWith("/revise")) {
      return "{\"revisedLineItemQuantities\":{\"1\":4}}";
    }
    return "{}";
  }

  private ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    objectMapper.findAndRegisterModules();
    return objectMapper;
  }
}