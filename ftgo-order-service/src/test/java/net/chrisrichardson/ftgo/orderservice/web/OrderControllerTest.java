package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.security.AuthenticatedConsumer;
import net.chrisrichardson.ftgo.common.security.StaffAuthenticator;
import net.chrisrichardson.ftgo.common.security.UnauthenticatedException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerAuthenticator;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

  private static final long CONSUMER_ID = 1511300065921L;
  private static final String AUTHORIZATION = "Bearer consumer-access-token";

  private OrderService orderService;
  private OrderRepository orderRepository;
  private ConsumerAuthenticator consumerAuthenticator;
  private StaffAuthenticator staffAuthenticator;
  private OrderController orderController;

  @Before
  public void setUp() throws Exception {
    orderService = mock(OrderService.class);
    orderRepository = mock(OrderRepository.class);
    consumerAuthenticator = mock(ConsumerAuthenticator.class);
    staffAuthenticator = mock(StaffAuthenticator.class);

    when(consumerAuthenticator.authenticate(AUTHORIZATION)).thenReturn(new AuthenticatedConsumer(CONSUMER_ID));
    when(consumerAuthenticator.authenticate(null)).thenThrow(new UnauthenticatedException("A consumer access token is required"));

    orderController = new OrderController(orderService, orderRepository, consumerAuthenticator, staffAuthenticator);
  }


  @Test
  public void shouldFindOrder() {

    when(orderRepository.findByIdAndConsumerId(1L, CONSUMER_ID)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
            header("Authorization", AUTHORIZATION).
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
    when(orderRepository.findByIdAndConsumerId(1L, CONSUMER_ID)).thenReturn(Optional.empty());

    given().
            standaloneSetup(configureControllers(orderController)).
            header("Authorization", AUTHORIZATION).
    when().
            get("/orders/1").
    then().
            statusCode(404)
    ;
  }

  @Test
  public void shouldNotFindOrderOfAnotherConsumer() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));
    when(orderRepository.findByIdAndConsumerId(eq(1L), eq(CONSUMER_ID + 1))).thenReturn(Optional.empty());
    when(consumerAuthenticator.authenticate("Bearer other-consumer-access-token"))
            .thenReturn(new AuthenticatedConsumer(CONSUMER_ID + 1));

    given().
            standaloneSetup(configureControllers(orderController)).
            header("Authorization", "Bearer other-consumer-access-token").
    when().
            get("/orders/1").
    then().
            statusCode(404)
    ;
  }

  @Test
  public void shouldNotCreateOrderForAnotherConsumer() {
    given().
            standaloneSetup(configureControllers(orderController)).
            header("Authorization", AUTHORIZATION).
            contentType("application/json").
            body(String.format("{\"consumerId\": %d, \"restaurantId\": 1, \"lineItems\": [{\"menuItemId\": \"1\", \"quantity\": 1}]}",
                    CONSUMER_ID + 1)).
    when().
            post("/orders").
    then().
            statusCode(403)
    ;

    verify(orderService, never()).createOrder(any(AuthenticatedConsumer.class), anyLong(), anyList());
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }

}
