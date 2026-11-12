package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.security.FtgoRole;
import net.chrisrichardson.ftgo.common.security.FtgoUser;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.Collections;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CONSUMER_ID;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.ORDER_ID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

  private static final long OTHER_CONSUMER_ID = CONSUMER_ID + 1;
  private static final long RESTAURANT_ID = 71L;
  private static final long OTHER_RESTAURANT_ID = RESTAURANT_ID + 1;
  private static final long COURIER_ID = 81L;
  private static final long OTHER_COURIER_ID = COURIER_ID + 1;

  private OrderService orderService;
  private OrderRepository orderRepository;
  private OrderController orderController;
  private Order order;
  private FtgoUser testUser;

  @Before
  public void setUp() throws Exception {
    orderService = mock(OrderService.class);
    orderRepository = mock(OrderRepository.class);
    orderController = new OrderController(orderService, orderRepository);
    Restaurant restaurant = new Restaurant(RESTAURANT_ID, "Test Restaurant", new RestaurantMenu(Collections.emptyList()));
    order = new Order(CONSUMER_ID, restaurant, OrderDetailsMother.chickenVindalooLineItems());
    order.setId(ORDER_ID);
    Courier courier = mock(Courier.class);
    when(courier.getId()).thenReturn(COURIER_ID);
    when(courier.actionsForDelivery(order)).thenReturn(Collections.emptyList());
    order.schedule(courier);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
  }

  @After
  public void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void shouldFindOrderAsOwningConsumer() {
    authenticate(FtgoRole.CONSUMER, CONSUMER_ID);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(OrderDetailsMother.CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
            when().
            get("/orders/1").
            then().
            statusCode(200).
            body("orderId", equalTo(new Long(OrderDetailsMother.ORDER_ID).intValue())).
            body("state", equalTo(OrderDetailsMother.CHICKEN_VINDALOO_ORDER_STATE.name())).
            body("orderTotal", equalTo(CHICKEN_VINDALOO_ORDER_TOTAL.asString()));
  }

  @Test
  public void shouldFindNotOrder() {
    authenticate(FtgoRole.CONSUMER, CONSUMER_ID);
    when(orderRepository.findById(1L)).thenReturn(Optional.empty());

    given().
            standaloneSetup(configureControllers(orderController)).
            when().
            get("/orders/1").
            then().
            statusCode(404);
  }

  @Test
  public void otherConsumerCannotReadOrChangeOrder() {
    authenticate(FtgoRole.CONSUMER, OTHER_CONSUMER_ID);
    assertOrderRoutesReturn(404);
    verifyZeroInteractions(orderService);
  }

  @Test
  public void owningConsumerCanCancelAndReviseButCannotActAsStaffOrCourier() {
    authenticate(FtgoRole.CONSUMER, CONSUMER_ID);
    when(orderService.cancel(ORDER_ID)).thenReturn(order);
    when(orderService.reviseOrder(eq(ORDER_ID), any())).thenReturn(order);

    assertStatus("POST", orderPath("cancel"), "{}", 200);
    assertStatus("POST", orderPath("revise"), "{}", 200);
    clearInvocations(orderService);
    for (String action : new String[]{"accept", "preparing", "ready", "pickedup", "delivered"}) {
      assertStatus("POST", orderPath(action), "{}", 403);
    }
    verifyZeroInteractions(orderService);
  }

  @Test
  public void restaurantCanPerformRestaurantActions() {
    authenticate(FtgoRole.RESTAURANT, RESTAURANT_ID);
    assertStatus("POST", orderPath("accept"), "{}", 200);
    assertStatus("POST", orderPath("preparing"), "{}", 200);
    assertStatus("POST", orderPath("ready"), "{}", 200);
    assertStatus("POST", orderPath("pickedup"), "{}", 403);
    verify(orderService).accept(eq(ORDER_ID), any());
    verify(orderService).notePreparing(ORDER_ID);
    verify(orderService).noteReadyForPickup(ORDER_ID);
    verify(orderService, org.mockito.Mockito.never()).notePickedUp(ORDER_ID);
  }

  @Test
  public void otherRestaurantCannotViewOrChangeOrder() {
    authenticate(FtgoRole.RESTAURANT, OTHER_RESTAURANT_ID);
    assertStatus("POST", orderPath("accept"), "{}", 404);
    verifyZeroInteractions(orderService);
  }

  @Test
  public void restaurantCannotPerformCourierActions() {
    authenticate(FtgoRole.RESTAURANT, RESTAURANT_ID);
    assertStatus("POST", orderPath("pickedup"), "{}", 403);
    verifyZeroInteractions(orderService);
  }

  @Test
  public void assignedCourierCanPickUpAndDeliver() {
    authenticate(FtgoRole.COURIER, COURIER_ID);
    assertStatus("POST", orderPath("pickedup"), "{}", 200);
    assertStatus("POST", orderPath("delivered"), "{}", 200);
    verify(orderService).notePickedUp(ORDER_ID);
    verify(orderService).noteDelivered(ORDER_ID);
  }

  @Test
  public void otherCourierCannotViewOrChangeOrder() {
    authenticate(FtgoRole.COURIER, OTHER_COURIER_ID);
    assertStatus("POST", orderPath("pickedup"), "{}", 404);
    verifyZeroInteractions(orderService);
  }

  @Test
  public void adminCanReadAndChangeOrder() {
    authenticate(FtgoRole.ADMIN, null);
    when(orderService.cancel(ORDER_ID)).thenReturn(order);
    assertStatus("GET", orderPath(null), null, 200);
    assertStatus("POST", orderPath("cancel"), "{}", 200);
    verify(orderService).cancel(ORDER_ID);
  }

  @Test
  public void consumerOrderListUsesOnlyItsOwnConsumerId() {
    authenticate(FtgoRole.CONSUMER, CONSUMER_ID);
    assertStatus("GET", "/orders?consumerId=" + OTHER_CONSUMER_ID, null, 403);
    verifyZeroInteractions(orderRepository);

    when(orderRepository.findAllByConsumerId(CONSUMER_ID)).thenReturn(Collections.emptyList());
    assertStatus("GET", "/orders", null, 200);
    verify(orderRepository).findAllByConsumerId(CONSUMER_ID);
  }

  @Test
  public void adminMustSupplyConsumerIdForOrderList() {
    authenticate(FtgoRole.ADMIN, null);
    assertStatus("GET", "/orders", null, 400);
    verifyZeroInteractions(orderRepository);
  }

  @Test
  public void onlyConsumersAndAdminsCanCreateOrders() {
    authenticate(FtgoRole.CONSUMER, CONSUMER_ID);
    when(orderService.createOrder(eq(CONSUMER_ID), eq(RESTAURANT_ID), anyList())).thenReturn(order);
    String request = "{\"consumerId\":" + OTHER_CONSUMER_ID + ",\"restaurantId\":" + RESTAURANT_ID + ",\"lineItems\":[]}";
    assertStatus("POST", "/orders", request, 403);
    verifyZeroInteractions(orderService);

    request = "{\"consumerId\":" + CONSUMER_ID + ",\"restaurantId\":" + RESTAURANT_ID + ",\"lineItems\":[]}";
    assertStatus("POST", "/orders", request, 200);
    verify(orderService).createOrder(eq(CONSUMER_ID), eq(RESTAURANT_ID), anyList());

    clearInvocations(orderService);
    SecurityContextHolder.clearContext();
    authenticate(FtgoRole.RESTAURANT, RESTAURANT_ID);
    assertStatus("POST", "/orders", request, 403);
    SecurityContextHolder.clearContext();
    authenticate(FtgoRole.COURIER, COURIER_ID);
    assertStatus("POST", "/orders", request, 403);
    verifyZeroInteractions(orderService);
  }

  private void assertOrderRoutesReturn(int expectedStatus) {
    assertStatus("GET", orderPath(null), null, expectedStatus);
    for (String action : new String[]{"cancel", "revise", "accept", "preparing", "ready", "pickedup", "delivered"}) {
      assertStatus("POST", orderPath(action), "{}", expectedStatus);
    }
  }

  private String orderPath(String action) {
    return "/orders/" + ORDER_ID + (action == null ? "" : "/" + action);
  }

  private void assertStatus(String method, String path, String body, int status) {
    restoreSecurityContext();
    if (HttpMethod.GET.matches(method)) {
      given().standaloneSetup(configureControllers(orderController)).
              when().get(path).
              then().statusCode(status);
    } else {
      given().standaloneSetup(configureControllers(orderController)).
              contentType("application/json").
              body(body).
              when().post(path).
              then().statusCode(status);
    }
  }

  private void authenticate(FtgoRole role, Long subjectId) {
    testUser = new FtgoUser("test", "{noop}password", role, subjectId);
    restoreSecurityContext();
  }

  private void restoreSecurityContext() {
    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(testUser, testUser.getPassword(), testUser.getAuthorities()));
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
