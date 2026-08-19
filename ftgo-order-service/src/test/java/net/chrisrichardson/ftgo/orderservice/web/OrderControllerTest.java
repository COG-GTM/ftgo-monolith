package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerAuthenticationException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerAuthenticator;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.Collections;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

  private static final long AUTHENTICATED_CONSUMER_ID = 101L;

  private OrderService orderService;
  private OrderRepository orderRepository;
  private ConsumerAuthenticator consumerAuthenticator;
  private OrderController orderController;

  @Before
  public void setUp() throws Exception {
    orderService = mock(OrderService.class);
    orderRepository = mock(OrderRepository.class);
    consumerAuthenticator = mock(ConsumerAuthenticator.class);
    orderController = new OrderController(orderService, orderRepository, consumerAuthenticator);
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
            standaloneSetup(configureControllers(new OrderController(orderService, orderRepository, consumerAuthenticator))).
    when().
            get("/orders/1").
    then().
            statusCode(404)
    ;
  }

  @Test
  public void shouldRejectOrderHistoryRequestWithoutApiKey() {
    when(consumerAuthenticator.authenticatedConsumerId(any()))
            .thenThrow(new ConsumerAuthenticationException("An API key is required"));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=1").
    then().
            statusCode(401)
    ;

    verify(orderRepository, never()).findAllByConsumerId(any(), any());
  }

  @Test
  public void shouldRejectOrderHistoryRequestForAnotherConsumer() {
    when(consumerAuthenticator.authenticatedConsumerId(any())).thenReturn(AUTHENTICATED_CONSUMER_ID);

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=" + (AUTHENTICATED_CONSUMER_ID + 1)).
    then().
            statusCode(403)
    ;

    verify(orderRepository, never()).findAllByConsumerId(any(), any());
  }

  @Test
  public void shouldReturnOrderHistoryOfAuthenticatedConsumer() {
    when(consumerAuthenticator.authenticatedConsumerId(any())).thenReturn(AUTHENTICATED_CONSUMER_ID);
    Page<Order> page = new PageImpl<>(Collections.singletonList(CHICKEN_VINDALOO_ORDER));
    when(orderRepository.findAllByConsumerId(eq(AUTHENTICATED_CONSUMER_ID), any(Pageable.class))).thenReturn(page);

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders").
    then().
            statusCode(200).
            body("[0].orderId", equalTo(new Long(OrderDetailsMother.ORDER_ID).intValue()))
    ;
  }

  @Test
  public void shouldCapThePageSizeOfTheOrderHistory() {
    when(consumerAuthenticator.authenticatedConsumerId(any())).thenReturn(AUTHENTICATED_CONSUMER_ID);
    when(orderRepository.findAllByConsumerId(eq(AUTHENTICATED_CONSUMER_ID), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?size=5000").
    then().
            statusCode(200)
    ;

    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(orderRepository).findAllByConsumerId(eq(AUTHENTICATED_CONSUMER_ID), pageable.capture());
    assertEquals(100, pageable.getValue().getPageSize());
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }

}