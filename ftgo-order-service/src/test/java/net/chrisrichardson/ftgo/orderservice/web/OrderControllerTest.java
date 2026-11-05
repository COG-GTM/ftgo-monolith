package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.security.ConsumerAuthenticationArgumentResolver;
import net.chrisrichardson.ftgo.common.security.ConsumerTokenService;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

  private static final String TEST_SECRET = "0123456789abcdef0123456789abcdef";
  private OrderService orderService;
  private OrderRepository orderRepository;
  private OrderController orderController;
  private ConsumerTokenService tokenService;

  @Before
  public void setUp() throws Exception {
    orderService = mock(OrderService.class);
    orderRepository = mock(OrderRepository.class);
    orderController = new OrderController(orderService, orderRepository);
    tokenService = new ConsumerTokenService(TEST_SECRET, Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC));
  }


  @Test
  public void shouldFindOrder() {

    when(orderRepository.findById(1L)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            header("Authorization", "Bearer " + tokenFor(OrderDetailsMother.CONSUMER_ID)).
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
            header("Authorization", "Bearer " + tokenFor(OrderDetailsMother.CONSUMER_ID)).
            standaloneSetup(configureControllers(new OrderController(orderService, orderRepository))).
    when().
            get("/orders/1").
    then().
            statusCode(404)
    ;
  }

  @Test
  public void shouldRejectOrderReadWithoutTokenAndChallengeBearer() {
    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders/1").
    then().
            statusCode(401).
            header("WWW-Authenticate", equalTo("Bearer"));
  }

  @Test
  public void shouldRejectInvalidOrderReadToken() {
    ConsumerTokenService otherService = new ConsumerTokenService("abcdef0123456789abcdef0123456789",
            Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC));

    given().
            header("Authorization", "Bearer " + otherService.issue(OrderDetailsMother.CONSUMER_ID, Duration.ofMinutes(30))).
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders/1").
    then().
            statusCode(401);
  }

  @Test
  public void shouldNotFindAnotherConsumersOrder() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            header("Authorization", "Bearer " + tokenFor(OrderDetailsMother.CONSUMER_ID + 1)).
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders/1").
    then().
            statusCode(404);
  }

  @Test
  public void shouldNotFindMissingOrderForAuthenticatedConsumer() {
    when(orderRepository.findById(1L)).thenReturn(Optional.empty());

    given().
            header("Authorization", "Bearer " + tokenFor(OrderDetailsMother.CONSUMER_ID)).
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders/1").
    then().
            statusCode(404);
  }

  @Test
  public void shouldRejectOtherConsumerQueryWithoutCallingRepository() {
    given().
            header("Authorization", "Bearer " + tokenFor(OrderDetailsMother.CONSUMER_ID)).
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=" + (OrderDetailsMother.CONSUMER_ID + 1)).
    then().
            statusCode(403);

    verify(orderRepository, never()).findAllByConsumerId(any());
  }

  @Test
  public void shouldFindOrdersForAuthenticatedConsumer() {
    when(orderRepository.findAllByConsumerId(OrderDetailsMother.CONSUMER_ID))
            .thenReturn(Collections.singletonList(CHICKEN_VINDALOO_ORDER));

    given().
            header("Authorization", "Bearer " + tokenFor(OrderDetailsMother.CONSUMER_ID)).
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=" + OrderDetailsMother.CONSUMER_ID).
    then().
            statusCode(200).
            body("[0].orderId", equalTo(new Long(OrderDetailsMother.ORDER_ID).intValue()));
  }

  @Test
  public void shouldFindCallerOrdersWhenConsumerIdIsOmitted() {
    when(orderRepository.findAllByConsumerId(OrderDetailsMother.CONSUMER_ID))
            .thenReturn(Collections.singletonList(CHICKEN_VINDALOO_ORDER));

    given().
            header("Authorization", "Bearer " + tokenFor(OrderDetailsMother.CONSUMER_ID)).
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders").
    then().
            statusCode(200).
            body("[0].orderId", equalTo(new Long(OrderDetailsMother.ORDER_ID).intValue()));
  }

  @Test
  public void shouldRejectOrderListWithoutToken() {
    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders").
    then().
            statusCode(401);
  }

  private String tokenFor(long consumerId) {
    return tokenService.issue(consumerId, Duration.ofMinutes(30));
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers)
            .setMessageConverters(converter)
            .setCustomArgumentResolvers(new ConsumerAuthenticationArgumentResolver(tokenService));
  }

}