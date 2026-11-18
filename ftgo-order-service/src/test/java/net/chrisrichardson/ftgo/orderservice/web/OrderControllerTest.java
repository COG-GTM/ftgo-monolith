package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.DistanceOptimizedCourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.chickenVindalooLineItems;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerTest {

  private static final long ORDER_ID = 42L;
  private static final String ORDER_PATH = "/orders/" + ORDER_ID;
  private static final String RESTAURANT_NAME = "Ajanta";
  private static final String[] LIFECYCLE_ACTIONS = {"accept", "preparing", "ready", "pickedup", "delivered"};

  private OrderRepository orderRepository;
  private OrderController orderController;
  private CourierRepository courierRepository;
  private MockMvc mockMvc;

  @Before
  public void setUp() throws Exception {
    orderRepository = mock(OrderRepository.class);
    courierRepository = mock(CourierRepository.class);
    OrderService orderService = new OrderService(orderRepository, null, Optional.empty(), null,
            courierRepository, new DistanceOptimizedCourierAssignmentStrategy());
    orderController = new OrderController(orderService, orderRepository);
    mockMvc = configureControllers(orderController).build();
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
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders/1").
    then().
            statusCode(404)
    ;
  }

  @Test
  public void shouldReturnUpdatedOrderAtEachLifecycleStep() throws Exception {
    stubApprovedOrder();
    Courier courier = spy(new Courier());
    when(courier.getId()).thenReturn(7L);
    when(courierRepository.findAllAvailable()).thenReturn(Collections.singletonList(courier));

    assertUpdatedOrder("accept", OrderState.ACCEPTED);
    assertUpdatedOrder("preparing", OrderState.PREPARING);
    assertUpdatedOrder("ready", OrderState.READY_FOR_PICKUP);
    assertUpdatedOrder("pickedup", OrderState.PICKED_UP);
    assertUpdatedOrder("delivered", OrderState.DELIVERED);
  }

  @Test
  public void shouldRejectMissingNullAndPastReadyBy() throws Exception {
    Order order = stubApprovedOrder();

    assertThatThrownBy(() -> mockMvc.perform(post(ORDER_PATH + "/accept")))
            .hasCauseInstanceOf(IllegalArgumentException.class);
    for (String body : new String[] {"{}", "{\"readyBy\":null}", readyByBody(LocalDateTime.now().minusDays(1))}) {
      assertThatThrownBy(() -> mockMvc.perform(post(ORDER_PATH + "/accept")
              .contentType(MediaType.APPLICATION_JSON).content(body)))
              .hasCauseInstanceOf(IllegalArgumentException.class);
    }
    assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
  }

  @Test
  public void shouldSignalMissingOrderOnEveryLifecycleStep() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

    for (String action : LIFECYCLE_ACTIONS) {
      assertThatThrownBy(() -> mockMvc.perform(lifecycleRequest(action)))
              .hasCauseInstanceOf(OrderNotFoundException.class);
    }
  }

  @Test
  public void shouldSignalOutOfOrderLifecycleTransitions() {
    Order order = stubApprovedOrder();
    order.cancel();

    for (String action : LIFECYCLE_ACTIONS) {
      assertThatThrownBy(() -> mockMvc.perform(lifecycleRequest(action)))
              .hasCauseInstanceOf(UnsupportedStateTransitionException.class);
    }
  }

  private Order stubApprovedOrder() {
    Restaurant restaurant = new Restaurant(1L, RESTAURANT_NAME, new RestaurantMenu(Collections.emptyList()));
    Order order = new Order(1L, restaurant, chickenVindalooLineItems());
    order.setId(ORDER_ID);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    return order;
  }

  private void assertUpdatedOrder(String action, OrderState state) throws Exception {
    String response = mockMvc.perform(lifecycleRequest(action))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(jsonPath("$.orderId").value((int) ORDER_ID))
            .andExpect(jsonPath("$.state").value(state.name()))
            .andExpect(jsonPath("$.orderTotal").value(CHICKEN_VINDALOO_ORDER_TOTAL.asString()))
            .andExpect(jsonPath("$.restaurantName").value(RESTAURANT_NAME))
            .andExpect(jsonPath("$.assignedCourier").value(7))
            .andExpect(jsonPath("$.courierActions[0].type").value("PICKUP"))
            .andExpect(jsonPath("$.courierActions[1].type").value("DROPOFF"))
            .andExpect(jsonPath("$.estimatedDeliveryTime").isNotEmpty())
            .andReturn().getResponse().getContentAsString();
    mockMvc.perform(get(ORDER_PATH)).andExpect(status().isOk()).andExpect(content().json(response, true));
  }

  private MockHttpServletRequestBuilder lifecycleRequest(String action) {
    MockHttpServletRequestBuilder request = post(ORDER_PATH + "/" + action);
    return "accept".equals(action) ? request.contentType(MediaType.APPLICATION_JSON)
            .content(readyByBody(LocalDateTime.now().plusDays(1))) : request;
  }

  private String readyByBody(LocalDateTime readyBy) {
    return "{\"readyBy\":\"" + readyBy + "\"}";
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }

}