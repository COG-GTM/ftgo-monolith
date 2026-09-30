package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.Collections;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CONSUMER_ID;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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


  private static final long OTHER_CONSUMER_ID = CONSUMER_ID + 1;

  private static Authentication consumer(long consumerId) {
    ConsumerUserDetails userDetails = new ConsumerUserDetails(consumerId, "[REDACTED]");
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  @Test
  public void shouldFindOrder() {

    when(orderRepository.findById(1L)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
            auth().principal(consumer(CONSUMER_ID)).
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
            auth().principal(consumer(CONSUMER_ID)).
    when().
            get("/orders/1").
    then().
            statusCode(404)
    ;
  }

  @Test
  public void shouldNotFindOrderOfAnotherConsumer() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
            auth().principal(consumer(OTHER_CONSUMER_ID)).
    when().
            get("/orders/1").
    then().
            statusCode(404)
    ;
  }

  @Test
  public void shouldRejectUnauthenticatedGetOrder() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders/1").
    then().
            statusCode(401)
    ;
  }

  @Test
  public void shouldFindOrdersOfAuthenticatedConsumer() {
    when(orderRepository.findAllByConsumerId(CONSUMER_ID)).thenReturn(Collections.singletonList(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
            auth().principal(consumer(CONSUMER_ID)).
    when().
            get("/orders?consumerId=" + CONSUMER_ID).
    then().
            statusCode(200).
            body("size()", equalTo(1)).
            body("[0].orderId", equalTo(new Long(OrderDetailsMother.ORDER_ID).intValue()))
    ;
  }

  @Test
  public void shouldRejectOrdersOfAnotherConsumer() {
    when(orderRepository.findAllByConsumerId(CONSUMER_ID)).thenReturn(Collections.singletonList(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
            auth().principal(consumer(OTHER_CONSUMER_ID)).
    when().
            get("/orders?consumerId=" + CONSUMER_ID).
    then().
            statusCode(403)
    ;
  }

  @Test
  public void shouldRejectUnauthenticatedGetOrders() {
    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=" + CONSUMER_ID).
    then().
            statusCode(401)
    ;
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }

}