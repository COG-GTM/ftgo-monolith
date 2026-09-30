package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.security.FtgoPrincipal;
import net.chrisrichardson.ftgo.common.security.FtgoRole;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.RestaurantMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.Collections;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CONSUMER_ID;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.ORDER_ID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

  private static final long OTHER_ID = 424242L;
  private static final long COURIER_ID = 77L;

  private OrderService orderService;
  private OrderRepository orderRepository;
  private OrderController orderController;

  @Before
  public void setUp() throws Exception {
    orderService = mock(OrderService.class);
    orderRepository = mock(OrderRepository.class);
    orderController = new OrderController(orderService, orderRepository, new OrderAccessPolicy());
  }

  @After
  public void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateAs(FtgoRole role, Long actorId) {
    FtgoPrincipal principal = new FtgoPrincipal(role.name().toLowerCase(), "{noop}pw", role, actorId);
    SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities()));
  }

  private Order orderAssignedToCourier(long courierId) {
    Courier courier = mock(Courier.class);
    when(courier.getId()).thenReturn(courierId);
    Order order = new Order(CONSUMER_ID, new Restaurant(RestaurantMother.AJANTA_ID, "", new RestaurantMenu(Collections.emptyList())),
            OrderDetailsMother.chickenVindalooLineItems());
    order.setId(ORDER_ID);
    order.schedule(courier);
    return order;
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
            standaloneSetup(configureControllers(new OrderController(orderService, orderRepository, new OrderAccessPolicy()))).
    when().
            get("/orders/1").
    then().
            statusCode(404)
    ;
  }

  @Test
  public void shouldLetConsumerCancelOwnOrder() {
    authenticateAs(FtgoRole.CONSUMER, CONSUMER_ID);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));
    when(orderService.cancel(ORDER_ID)).thenReturn(CHICKEN_VINDALOO_ORDER);

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/" + ORDER_ID + "/cancel").
    then().
            statusCode(200).
            body("orderId", equalTo((int) ORDER_ID));

    verify(orderService).cancel(ORDER_ID);
  }

  @Test
  public void shouldForbidConsumerCancellingSomeoneElsesOrder() {
    authenticateAs(FtgoRole.CONSUMER, OTHER_ID);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/" + ORDER_ID + "/cancel").
    then().
            statusCode(403);

    verify(orderService, never()).cancel(anyLong());
  }

  @Test
  public void shouldForbidCancelWithoutPrincipal() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/" + ORDER_ID + "/cancel").
    then().
            statusCode(403);

    verify(orderService, never()).cancel(anyLong());
  }

  @Test
  public void shouldForbidRestaurantCancellingOrder() {
    authenticateAs(FtgoRole.RESTAURANT, RestaurantMother.AJANTA_ID);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
            contentType("application/json").
            body("{\"revisedLineItemQuantities\": {\"1\": 3}}").
    when().
            post("/orders/" + ORDER_ID + "/revise").
    then().
            statusCode(403);

    verify(orderService, never()).reviseOrder(anyLong(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void shouldLetOwningRestaurantStartPreparing() {
    authenticateAs(FtgoRole.RESTAURANT, RestaurantMother.AJANTA_ID);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/" + ORDER_ID + "/preparing").
    then().
            statusCode(200);

    verify(orderService).notePreparing(ORDER_ID);
  }

  @Test
  public void shouldForbidOtherRestaurantStartingPreparing() {
    authenticateAs(FtgoRole.RESTAURANT, OTHER_ID);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/" + ORDER_ID + "/preparing").
    then().
            statusCode(403);

    verify(orderService, never()).notePreparing(anyLong());
  }

  @Test
  public void shouldForbidConsumerAcceptingOrder() {
    authenticateAs(FtgoRole.CONSUMER, CONSUMER_ID);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
            contentType("application/json").
            body("{\"readyBy\": \"2030-01-01T10:00:00\"}").
    when().
            post("/orders/" + ORDER_ID + "/accept").
    then().
            statusCode(403);

    verify(orderService, never()).accept(anyLong(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void shouldLetAssignedCourierMarkDelivered() {
    authenticateAs(FtgoRole.COURIER, COURIER_ID);
    Order order = orderAssignedToCourier(COURIER_ID);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/" + ORDER_ID + "/delivered").
    then().
            statusCode(200);

    verify(orderService).noteDelivered(ORDER_ID);
  }

  @Test
  public void shouldForbidUnassignedCourierMarkingDelivered() {
    authenticateAs(FtgoRole.COURIER, OTHER_ID);
    Order order = orderAssignedToCourier(COURIER_ID);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/" + ORDER_ID + "/pickedup").
    then().
            statusCode(403);

    verify(orderService, never()).notePickedUp(anyLong());
  }

  @Test
  public void shouldLetAdminAdvanceAnyOrder() {
    authenticateAs(FtgoRole.ADMIN, null);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/" + ORDER_ID + "/ready").
    then().
            statusCode(200);

    verify(orderService).noteReadyForPickup(ORDER_ID);
  }

  @Test
  public void shouldReturnNotFoundForUnknownOrderTransition() {
    authenticateAs(FtgoRole.ADMIN, null);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/" + ORDER_ID + "/preparing").
    then().
            statusCode(404);

    verify(orderService, never()).notePreparing(anyLong());
  }

  @Test
  public void shouldForbidConsumerCreatingOrderForSomeoneElse() {
    authenticateAs(FtgoRole.CONSUMER, OTHER_ID);

    given().
            standaloneSetup(configureControllers(orderController)).
            contentType("application/json").
            body("{\"consumerId\": " + CONSUMER_ID + ", \"restaurantId\": 1, \"lineItems\": [{\"menuItemId\": \"1\", \"quantity\": 2}]}").
    when().
            post("/orders").
    then().
            statusCode(403);

    verify(orderService, never()).createOrder(anyLong(), anyLong(), anyList());
  }

  @Test
  public void shouldLetConsumerCreateOwnOrder() {
    authenticateAs(FtgoRole.CONSUMER, CONSUMER_ID);
    when(orderService.createOrder(anyLong(), anyLong(), anyList())).thenReturn(CHICKEN_VINDALOO_ORDER);

    given().
            standaloneSetup(configureControllers(orderController)).
            contentType("application/json").
            body("{\"consumerId\": " + CONSUMER_ID + ", \"restaurantId\": 1, \"lineItems\": [{\"menuItemId\": \"1\", \"quantity\": 2}]}").
    when().
            post("/orders").
    then().
            statusCode(200).
            body("orderId", equalTo((int) ORDER_ID));
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    objectMapper.registerModule(new JavaTimeModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers)
            .setMessageConverters(converter)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver());
  }

}
