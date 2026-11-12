package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.domain.Order;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Collections;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

  private OrderService orderService;
  private OrderRepository orderRepository;
  private OrderController orderController;
  private LocalValidatorFactoryBean validator;

  @Before
  public void setUp() throws Exception {
    orderService = mock(OrderService.class);
    orderRepository = mock(OrderRepository.class);
    orderController = new OrderController(orderService, orderRepository);
    validator = new LocalValidatorFactoryBean();
    validator.setMessageInterpolator(new ParameterMessageInterpolator());
    validator.afterPropertiesSet();
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
  public void shouldRejectCreateOrderWithZeroQuantity() {
    postOrder("{\"consumerId\":1,\"restaurantId\":2,\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":0}]}")
            .then()
            .statusCode(400);

    verifyZeroInteractions(orderService);
  }

  @Test
  public void shouldRejectCreateOrderWithEmptyLineItems() {
    postOrder("{\"consumerId\":1,\"restaurantId\":2,\"lineItems\":[]}")
            .then()
            .statusCode(400);

    verifyZeroInteractions(orderService);
  }

  @Test
  public void shouldRejectCreateOrderWithMoreThanOneHundredLineItems() {
    StringBuilder body = new StringBuilder("{\"consumerId\":1,\"restaurantId\":2,\"lineItems\":[");
    for (int i = 0; i < 101; i++) {
      if (i > 0) {
        body.append(',');
      }
      body.append("{\"menuItemId\":\"").append(i).append("\",\"quantity\":1}");
    }
    body.append("]}");

    postOrder(body.toString()).then().statusCode(400);

    verifyZeroInteractions(orderService);
  }

  @Test
  public void shouldCreateValidOrder() {
    Order order = mock(Order.class);
    when(order.getId()).thenReturn(10L);
    when(orderService.createOrder(eq(1L), eq(2L), anyList())).thenReturn(order);

    postOrder("{\"consumerId\":1,\"restaurantId\":2,\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":1}]}")
            .then()
            .statusCode(200);

    verify(orderService).createOrder(eq(1L), eq(2L), anyList());
  }

  @Test
  public void shouldQueryOrdersUsingDefaultPageAndSizeSortedNewestFirst() {
    when(orderRepository.findAllByConsumerId(eq(1L), any(Pageable.class)))
            .thenReturn(Collections.emptyList());

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=1").
    then().
            statusCode(200)
    ;

    org.mockito.ArgumentCaptor<Pageable> pageableCaptor =
            org.mockito.ArgumentCaptor.forClass(Pageable.class);
    verify(orderRepository).findAllByConsumerId(eq(1L), pageableCaptor.capture());
    Assert.assertEquals(PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id")), pageableCaptor.getValue());
  }

  @Test
  public void shouldRejectOrdersRequestWithOversizedPage() {
    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=1&size=101").
    then().
            statusCode(400)
    ;

    verifyZeroInteractions(orderRepository);
  }

  @Test
  public void shouldRejectOrdersRequestWithNegativePage() {
    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=1&page=-1").
    then().
            statusCode(400)
    ;

    verifyZeroInteractions(orderRepository);
  }

  @Test
  public void shouldRejectOrdersRequestWithZeroSize() {
    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=1&size=0").
    then().
            statusCode(400)
    ;

    verifyZeroInteractions(orderRepository);
  }

  private io.restassured.module.mockmvc.response.MockMvcResponse postOrder(String body) {
    return given().
            standaloneSetup(configureControllers(orderController)).
            contentType("application/json").
            body(body).
    when().
            post("/orders");
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers)
            .setMessageConverters(converter)
            .setValidator(validator);
  }

}