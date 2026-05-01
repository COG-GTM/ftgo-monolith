package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.*;

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
  public void shouldCreateOrder() {
    Order order = new Order(1L,
            new Restaurant(1L, "Ajanta", new RestaurantMenu(Collections.emptyList())),
            Collections.emptyList());
    order.setId(99L);

    when(orderService.createOrder(anyLong(), anyLong(), anyList())).thenReturn(order);

    given().
            standaloneSetup(configureControllers(orderController)).
            contentType("application/json").
            body("{\"consumerId\":1,\"restaurantId\":1,\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":2}]}").
    when().
            post("/orders").
    then().
            statusCode(200).
            body("orderId", equalTo(99))
    ;
  }

  @Test
  public void shouldCancelOrder() {
    Order order = new Order(1L,
            new Restaurant(1L, "Ajanta", new RestaurantMenu(Collections.emptyList())),
            Collections.emptyList());
    order.setId(1L);

    when(orderService.cancel(1L)).thenReturn(order);

    given().
            standaloneSetup(configureControllers(orderController)).
            contentType("application/json").
    when().
            post("/orders/1/cancel").
    then().
            statusCode(200)
    ;
  }

  @Test
  public void shouldAcceptOrder() {
    doNothing().when(orderService).accept(anyLong(), any(LocalDateTime.class));

    given().
            standaloneSetup(configureControllers(orderController)).
            contentType("application/json").
            body("{\"readyBy\":\"2026-12-01T12:00:00\"}").
    when().
            post("/orders/1/accept").
    then().
            statusCode(200)
    ;
  }

  @Test
  public void shouldReturnOrdersForConsumer() {
    when(orderRepository.findAllByConsumerId(1L)).thenReturn(Collections.singletonList(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=1").
    then().
            statusCode(200).
            body("size()", equalTo(1))
    ;
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    objectMapper.registerModule(new JavaTimeModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }

}
