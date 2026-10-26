package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.orderservice.web.security.FtgoPrincipal;
import net.chrisrichardson.ftgo.orderservice.web.security.FtgoRole;
import net.chrisrichardson.ftgo.orderservice.web.security.OrderAccessPolicy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

  private OrderService orderService;
  private OrderRepository orderRepository;
  private OrderController orderController;

  @Before
  public void setUp() throws Exception {
    orderService = mock(OrderService.class);
    orderRepository = mock(OrderRepository.class);
    orderController = new OrderController(orderService, orderRepository, new OrderAccessPolicy(orderRepository));
  }

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateAs(FtgoRole role, long id) {
    FtgoPrincipal principal = new FtgoPrincipal(role.name().toLowerCase() + "-" + id, role, id);
    SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, AuthorityUtils.createAuthorityList(role.authority())));
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
            standaloneSetup(configureControllers(new OrderController(orderService, orderRepository, new OrderAccessPolicy(orderRepository)))).
    when().
            get("/orders/1").
    then().
            statusCode(404)
    ;
  }

  @Test
  public void shouldRejectCancelWithoutAuthentication() {
    when(orderRepository.findById(OrderDetailsMother.ORDER_ID)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/" + OrderDetailsMother.ORDER_ID + "/cancel").
    then().
            statusCode(403);

    verify(orderService, never()).cancel(anyLong());
  }

  @Test
  public void shouldRejectCancelByOtherConsumer() {
    when(orderRepository.findById(OrderDetailsMother.ORDER_ID)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));
    authenticateAs(FtgoRole.CONSUMER, OrderDetailsMother.CONSUMER_ID + 1);

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/" + OrderDetailsMother.ORDER_ID + "/cancel").
    then().
            statusCode(403);

    verify(orderService, never()).cancel(anyLong());
  }

  @Test
  public void shouldCancelOwnOrder() {
    when(orderRepository.findById(OrderDetailsMother.ORDER_ID)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));
    when(orderService.cancel(OrderDetailsMother.ORDER_ID)).thenReturn(CHICKEN_VINDALOO_ORDER);
    authenticateAs(FtgoRole.CONSUMER, OrderDetailsMother.CONSUMER_ID);

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/" + OrderDetailsMother.ORDER_ID + "/cancel").
    then().
            statusCode(200).
            body("orderId", equalTo(new Long(OrderDetailsMother.ORDER_ID).intValue()));

    verify(orderService).cancel(OrderDetailsMother.ORDER_ID);
  }

  @Test
  public void shouldRejectCreateForOtherConsumer() {
    authenticateAs(FtgoRole.CONSUMER, OrderDetailsMother.CONSUMER_ID + 1);

    given().
            standaloneSetup(configureControllers(orderController)).
            contentType("application/json").
            body("{\"consumerId\":" + OrderDetailsMother.CONSUMER_ID + ",\"restaurantId\":1,\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":1}]}").
    when().
            post("/orders").
    then().
            statusCode(403);

    verify(orderService, never()).createOrder(anyLong(), anyLong(), org.mockito.ArgumentMatchers.any());
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }

}