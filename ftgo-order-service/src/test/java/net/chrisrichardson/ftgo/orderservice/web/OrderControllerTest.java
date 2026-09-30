package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.orderservice.security.FtgoRoles;
import net.chrisrichardson.ftgo.orderservice.security.FtgoUser;
import net.chrisrichardson.ftgo.orderservice.security.OrderAccessPolicy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.Collections;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CONSUMER_ID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderControllerTest {

  private static final long OTHER_CONSUMER_ID = CONSUMER_ID + 1;

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
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void shouldFindOrder() {
    authenticateAsOperations();

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
    authenticateAsOperations();

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
  public void shouldFindOwnOrderAsConsumer() {
    authenticateAsConsumer(CONSUMER_ID);

    when(orderRepository.findById(1L)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders/1").
    then().
            statusCode(200).
            body("orderId", equalTo(new Long(OrderDetailsMother.ORDER_ID).intValue()))
    ;
  }

  @Test
  public void shouldForbidOtherConsumersOrder() {
    authenticateAsConsumer(OTHER_CONSUMER_ID);

    when(orderRepository.findById(1L)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders/1").
    then().
            statusCode(403)
    ;
  }

  @Test
  public void shouldForbidOrderWhenUnauthenticated() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders/1").
    then().
            statusCode(403)
    ;
  }

  @Test
  public void shouldListOrdersForAnyConsumerAsOperations() {
    authenticateAsOperations();

    when(orderRepository.findAllByConsumerId(CONSUMER_ID)).thenReturn(Collections.singletonList(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=" + CONSUMER_ID).
    then().
            statusCode(200).
            body("$", hasSize(1)).
            body("[0].orderId", equalTo(new Long(OrderDetailsMother.ORDER_ID).intValue()))
    ;
  }

  @Test
  public void shouldRequireConsumerIdForOperations() {
    authenticateAsOperations();

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders").
    then().
            statusCode(400)
    ;
  }

  @Test
  public void shouldScopeOrderListToAuthenticatedConsumer() {
    authenticateAsConsumer(CONSUMER_ID);

    when(orderRepository.findAllByConsumerId(CONSUMER_ID)).thenReturn(Collections.singletonList(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders").
    then().
            statusCode(200).
            body("$", hasSize(1))
    ;
  }

  @Test
  public void shouldForbidListingOtherConsumersOrders() {
    authenticateAsConsumer(OTHER_CONSUMER_ID);

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            get("/orders?consumerId=" + CONSUMER_ID).
    then().
            statusCode(403)
    ;

    verify(orderRepository, never()).findAllByConsumerId(CONSUMER_ID);
  }

  @Test
  public void shouldForbidCancellingOtherConsumersOrder() {
    authenticateAsConsumer(OTHER_CONSUMER_ID);

    when(orderRepository.findById(1L)).thenReturn(Optional.of(CHICKEN_VINDALOO_ORDER));

    given().
            standaloneSetup(configureControllers(orderController)).
    when().
            post("/orders/1/cancel").
    then().
            statusCode(403)
    ;

    verify(orderService, never()).cancel(1L);
  }

  private void authenticateAsOperations() {
    authenticateAs(new FtgoUser("ops", "", AuthorityUtils.createAuthorityList("ROLE_" + FtgoRoles.OPERATIONS), null));
  }

  private void authenticateAsConsumer(long consumerId) {
    authenticateAs(new FtgoUser("consumer", "", AuthorityUtils.createAuthorityList("ROLE_" + FtgoRoles.CONSUMER), consumerId));
  }

  private void authenticateAs(FtgoUser user) {
    SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }

}
