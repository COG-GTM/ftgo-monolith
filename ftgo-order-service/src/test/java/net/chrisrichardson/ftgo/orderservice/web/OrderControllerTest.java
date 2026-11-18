package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
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
  public void shouldFindOrdersForConsumerAsArray() {
    when(orderRepository.findAllByConsumerId(1L)).thenReturn(Collections.singletonList(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=1").
    then().
            statusCode(200).
            body("size()", equalTo(1)).
            body("[0].orderId", equalTo(new Long(OrderDetailsMother.ORDER_ID).intValue())).
            body("[0].state", equalTo(OrderDetailsMother.CHICKEN_VINDALOO_ORDER_STATE.name())).
            body("[0].orderTotal", equalTo(CHICKEN_VINDALOO_ORDER_TOTAL.asString()))
    ;

    verify(orderRepository, never()).findAllByConsumerId(anyLong(), any(Pageable.class));
  }

  @Test
  public void shouldFilterOrdersByState() {
    when(orderRepository.findAllByConsumerIdAndOrderState(1L, OrderState.APPROVED)).thenReturn(Collections.singletonList(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=1&state=APPROVED").
    then().
            statusCode(200).
            body("size()", equalTo(1)).
            body("[0].state", equalTo(OrderState.APPROVED.name()))
    ;

    verify(orderRepository, never()).findAllByConsumerId(1L);
  }

  @Test
  public void shouldAcceptStateCaseInsensitively() {
    when(orderRepository.findAllByConsumerIdAndOrderState(1L, OrderState.DELIVERED)).thenReturn(Collections.emptyList());

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=1&state=delivered").
    then().
            statusCode(200).
            body("size()", equalTo(0))
    ;
  }

  @Test
  public void shouldReturnPageWhenPageRequested() {
    when(orderRepository.findAllByConsumerId(eq(1L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(CHICKEN_VINDALOO_ORDER), Pageable.unpaged(), 25));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=1&page=2&size=10").
    then().
            statusCode(200).
            body("content.size()", equalTo(1)).
            body("content[0].orderId", equalTo(new Long(OrderDetailsMother.ORDER_ID).intValue())).
            body("page", equalTo(2)).
            body("size", equalTo(10)).
            body("totalElements", equalTo(25))
    ;

    Pageable pageable = capturePageable();
    assertEquals(2, pageable.getPageNumber());
    assertEquals(10, pageable.getPageSize());
    assertEquals(Sort.by("id"), pageable.getSort());
  }

  @Test
  public void shouldDefaultPageSizeTo20() {
    when(orderRepository.findAllByConsumerId(eq(1L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.<Order>emptyList(), Pageable.unpaged(), 0));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=1&page=0").
    then().
            statusCode(200).
            body("content.size()", equalTo(0)).
            body("page", equalTo(0)).
            body("size", equalTo(20)).
            body("totalElements", equalTo(0))
    ;

    assertEquals(20, capturePageable().getPageSize());
  }

  @Test
  public void shouldAllowMaxPageSize() {
    when(orderRepository.findAllByConsumerId(eq(1L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.<Order>emptyList(), Pageable.unpaged(), 0));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=1&page=0&size=100").
    then().
            statusCode(200).
            body("size", equalTo(100))
    ;
  }

  @Test
  public void shouldFilterPagedOrdersByState() {
    when(orderRepository.findAllByConsumerIdAndOrderState(eq(1L), eq(OrderState.APPROVED), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.singletonList(CHICKEN_VINDALOO_ORDER), Pageable.unpaged(), 1));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=1&state=APPROVED&page=0&size=5").
    then().
            statusCode(200).
            body("content[0].state", equalTo(OrderState.APPROVED.name())).
            body("totalElements", equalTo(1))
    ;
  }

  @Test
  public void shouldRejectMissingConsumerId() {
    assertBadRequest("/orders", "consumerId is required");
    assertBadRequest("/orders?consumerId=", "consumerId is required");
  }

  @Test
  public void shouldRejectNonNumericConsumerId() {
    assertBadRequest("/orders?consumerId=abc", "consumerId must be a number");
  }

  @Test
  public void shouldRejectUnknownState() {
    assertBadRequest("/orders?consumerId=1&state=BOGUS", "state must be one of");
  }

  @Test
  public void shouldRejectInvalidPage() {
    assertBadRequest("/orders?consumerId=1&page=-1", "page must be >= 0");
    assertBadRequest("/orders?consumerId=1&page=abc", "page must be an integer");
  }

  @Test
  public void shouldRejectInvalidSize() {
    assertBadRequest("/orders?consumerId=1&page=0&size=0", "size must be between 1 and 100");
    assertBadRequest("/orders?consumerId=1&page=0&size=101", "size must be between 1 and 100");
    assertBadRequest("/orders?consumerId=1&page=0&size=ten", "size must be an integer");
  }

  private void assertBadRequest(String path, String message) {
    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get(path).
    then().
            statusCode(400).
            body(containsString(message))
    ;
    verifyZeroInteractions(orderRepository);
  }

  private Pageable capturePageable() {
    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(orderRepository).findAllByConsumerId(eq(1L), captor.capture());
    return captor.getValue();
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers)
            .setControllerAdvice(new IllegalArgumentAdvice())
            .setMessageConverters(converter);
  }

  // Mirrors the application's GlobalExceptionHandler mapping of IllegalArgumentException to 400.
  @ControllerAdvice
  static class IllegalArgumentAdvice {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handle(IllegalArgumentException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

}
