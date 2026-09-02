package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.security.ApiRole;
import net.chrisrichardson.ftgo.common.security.RoleApiKeyAuthorizer;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

  private static final String RESTAURANT_KEY = "restaurant-key";
  private static final String COURIER_KEY = "courier-key";
  private static final String OPERATOR_KEY = "operator-key";

  private OrderService orderService;
  private OrderRepository orderRepository;
  private OrderController orderController;

  @Before
  public void setUp() throws Exception {
    orderService = mock(OrderService.class);
    orderRepository = mock(OrderRepository.class);
    Map<ApiRole, String> keys = new EnumMap<>(ApiRole.class);
    keys.put(ApiRole.RESTAURANT, RESTAURANT_KEY);
    keys.put(ApiRole.COURIER, COURIER_KEY);
    keys.put(ApiRole.OPERATOR, OPERATOR_KEY);
    orderController = new OrderController(orderService, orderRepository, new RoleApiKeyAuthorizer(keys));
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
  public void shouldRejectUnauthenticatedLifecycleTransition() {
    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/1/delivered").
    then().
            statusCode(401)
    ;

    verify(orderService, never()).noteDelivered(1L);
  }

  @Test
  public void shouldRejectInvalidApiKey() {
    given().
            header("Authorization", "Bearer not-a-real-key").
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/1/ready").
    then().
            statusCode(401)
    ;

    verify(orderService, never()).noteReadyForPickup(1L);
  }

  @Test
  public void shouldRejectWrongRoleForRestaurantTransition() {
    given().
            header("Authorization", "Bearer " + COURIER_KEY).
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/1/preparing").
    then().
            statusCode(403)
    ;

    verify(orderService, never()).notePreparing(1L);
  }

  @Test
  public void shouldRejectWrongRoleForCourierTransition() {
    given().
            header("Authorization", "Bearer " + RESTAURANT_KEY).
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/1/pickedup").
    then().
            statusCode(403)
    ;

    verify(orderService, never()).notePickedUp(1L);
  }

  @Test
  public void shouldRejectNonOperatorCancel() {
    given().
            header("Authorization", "Bearer " + RESTAURANT_KEY).
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/1/cancel").
    then().
            statusCode(403)
    ;

    verify(orderService, never()).cancel(1L);
  }

  @Test
  public void shouldAllowRestaurantToMarkOrderReady() {
    given().
            header("Authorization", "Bearer " + RESTAURANT_KEY).
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/1/ready").
    then().
            statusCode(200)
    ;

    verify(orderService).noteReadyForPickup(1L);
  }

  @Test
  public void shouldAllowCourierToMarkOrderDelivered() {
    given().
            header("Authorization", "Bearer " + COURIER_KEY).
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/1/delivered").
    then().
            statusCode(200)
    ;

    verify(orderService).noteDelivered(1L);
  }

  @Test
  public void shouldAllowOperatorToCancelOrder() {
    when(orderService.cancel(1L)).thenReturn(CHICKEN_VINDALOO_ORDER);

    given().
            header("Authorization", "Bearer " + OPERATOR_KEY).
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/1/cancel").
    then().
            statusCode(200)
    ;

    verify(orderService).cancel(1L);
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }

}