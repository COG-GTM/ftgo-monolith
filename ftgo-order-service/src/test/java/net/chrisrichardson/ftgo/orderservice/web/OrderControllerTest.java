package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.Collections;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CONSUMER_ID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
  public void shouldReturnOrdersForConsumerWhenOperationsRole() throws Exception {
    when(orderRepository.findAllByConsumerId(CONSUMER_ID)).thenReturn(Collections.singletonList(CHICKEN_VINDALOO_ORDER));

    configureControllers(orderController).build()
            .perform(get("/orders").param("consumerId", Long.toString(CONSUMER_ID))
                    .principal(operationsAuthentication()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].orderId").value((int) OrderDetailsMother.ORDER_ID));
  }

  @Test
  public void shouldReturnOrdersForMatchingConsumerPrincipal() throws Exception {
    when(orderRepository.findAllByConsumerId(CONSUMER_ID)).thenReturn(Collections.singletonList(CHICKEN_VINDALOO_ORDER));

    configureControllers(orderController).build()
            .perform(get("/orders").param("consumerId", Long.toString(CONSUMER_ID))
                    .principal(consumerAuthentication(CONSUMER_ID)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].orderId").value((int) OrderDetailsMother.ORDER_ID));
  }

  @Test
  public void shouldRejectOrderHistoryRequestForOtherConsumer() throws Exception {
    configureControllers(orderController).build()
            .perform(get("/orders").param("consumerId", Long.toString(CONSUMER_ID))
                    .principal(consumerAuthentication(CONSUMER_ID + 1)))
            .andExpect(status().isForbidden());
  }

  @Test
  public void shouldRejectUnauthenticatedOrderHistoryRequest() throws Exception {
    configureControllers(orderController).build()
            .perform(get("/orders").param("consumerId", Long.toString(CONSUMER_ID)))
            .andExpect(status().isForbidden());
  }

  private Authentication operationsAuthentication() {
    return new UsernamePasswordAuthenticationToken("operations", "",
            AuthorityUtils.createAuthorityList("ROLE_OPERATIONS"));
  }

  private Authentication consumerAuthentication(long consumerId) {
    return new UsernamePasswordAuthenticationToken(Long.toString(consumerId), "",
            AuthorityUtils.createAuthorityList("ROLE_CONSUMER"));
  }

  private StandaloneMockMvcBuilder configureControllers(Object... controllers) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new MoneyModule());
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
    return MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(converter);
  }

}