package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.RestaurantMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.orderservice.web.security.FtgoRole;
import net.chrisrichardson.ftgo.orderservice.web.security.OrderAccessPolicy;
import net.chrisrichardson.ftgo.orderservice.web.security.OrderSecurityConfiguration;
import net.chrisrichardson.ftgo.orderservice.web.security.TestJwtTokens;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CONSUMER_ID;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.ORDER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = OrderController.class)
@ContextConfiguration(classes = OrderControllerSecurityTest.Config.class)
@TestPropertySource(properties = "ftgo.security.jwt.secret=" + TestJwtTokens.TEST_SECRET)
public class OrderControllerSecurityTest {

  @Configuration
  @Import({OrderController.class, OrderAccessPolicy.class, OrderSecurityConfiguration.class})
  static class Config {
    @Bean
    public MoneyModule moneyModule() {
      return new MoneyModule();
    }
  }

  private static final long COURIER_ID = 77L;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private OrderService orderService;

  @MockBean
  private OrderRepository orderRepository;

  private static final String CREATE_ORDER_JSON = "{\"consumerId\":" + CONSUMER_ID + ",\"restaurantId\":" + RestaurantMother.AJANTA_ID
          + ",\"lineItems\":[{\"menuItemId\":\"" + RestaurantMother.CHICKEN_VINDALOO_MENU_ITEM_ID + "\",\"quantity\":1}]}";

  @Before
  public void setUp() {
    Courier courier = mock(Courier.class);
    when(courier.getId()).thenReturn(COURIER_ID);
    when(courier.actionsForDelivery(any())).thenReturn(Collections.emptyList());

    Order order = new Order(CONSUMER_ID, RestaurantMother.AJANTA_RESTAURANT, OrderDetailsMother.chickenVindalooLineItems());
    order.setId(ORDER_ID);
    order.schedule(courier);

    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    when(orderService.cancel(ORDER_ID)).thenReturn(order);
    when(orderService.createOrder(anyLong(), anyLong(), any())).thenReturn(order);
  }

  @Test
  public void shouldRejectUnauthenticatedCancel() throws Exception {
    mockMvc.perform(post("/orders/{orderId}/cancel", ORDER_ID))
            .andExpect(status().isUnauthorized());
    verify(orderService, never()).cancel(anyLong());
  }

  @Test
  public void shouldRejectCancelWithForgedToken() throws Exception {
    mockMvc.perform(post("/orders/{orderId}/cancel", ORDER_ID)
            .header("Authorization", "Bearer " + TestJwtTokens.token(TestJwtTokens.OTHER_SECRET, FtgoRole.CONSUMER, CONSUMER_ID,
                    new java.util.Date(System.currentTimeMillis() + 60_000))))
            .andExpect(status().isUnauthorized());
    verify(orderService, never()).cancel(anyLong());
  }

  @Test
  public void shouldRejectCancelByOtherConsumer() throws Exception {
    mockMvc.perform(post("/orders/{orderId}/cancel", ORDER_ID).header("Authorization", bearer(FtgoRole.CONSUMER, CONSUMER_ID + 1)))
            .andExpect(status().isForbidden());
    verify(orderService, never()).cancel(anyLong());
  }

  @Test
  public void shouldAllowCancelByOwner() throws Exception {
    mockMvc.perform(post("/orders/{orderId}/cancel", ORDER_ID).header("Authorization", bearer(FtgoRole.CONSUMER, CONSUMER_ID)))
            .andExpect(status().isOk());
    verify(orderService).cancel(ORDER_ID);
  }

  @Test
  public void shouldRejectUnauthenticatedCreate() throws Exception {
    mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(CREATE_ORDER_JSON))
            .andExpect(status().isUnauthorized());
    verify(orderService, never()).createOrder(anyLong(), anyLong(), any());
  }

  @Test
  public void shouldRejectCreateForOtherConsumer() throws Exception {
    mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(CREATE_ORDER_JSON)
            .header("Authorization", bearer(FtgoRole.CONSUMER, CONSUMER_ID + 1)))
            .andExpect(status().isForbidden());
    verify(orderService, never()).createOrder(anyLong(), anyLong(), any());
  }

  @Test
  public void shouldAllowCreateForSelf() throws Exception {
    mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(CREATE_ORDER_JSON)
            .header("Authorization", bearer(FtgoRole.CONSUMER, CONSUMER_ID)))
            .andExpect(status().isOk());
    verify(orderService).createOrder(anyLong(), anyLong(), any());
  }

  @Test
  public void shouldRejectAcceptByConsumer() throws Exception {
    mockMvc.perform(post("/orders/{orderId}/accept", ORDER_ID).contentType(MediaType.APPLICATION_JSON)
            .content("{\"readyBy\":\"2030-01-01T10:00:00\"}")
            .header("Authorization", bearer(FtgoRole.CONSUMER, CONSUMER_ID)))
            .andExpect(status().isForbidden());
    verify(orderService, never()).accept(anyLong(), any());
  }

  @Test
  public void shouldRejectAcceptByOtherRestaurant() throws Exception {
    mockMvc.perform(post("/orders/{orderId}/accept", ORDER_ID).contentType(MediaType.APPLICATION_JSON)
            .content("{\"readyBy\":\"2030-01-01T10:00:00\"}")
            .header("Authorization", bearer(FtgoRole.RESTAURANT, RestaurantMother.AJANTA_ID + 1)))
            .andExpect(status().isForbidden());
    verify(orderService, never()).accept(anyLong(), any());
  }

  @Test
  public void shouldAllowAcceptByOwningRestaurant() throws Exception {
    mockMvc.perform(post("/orders/{orderId}/accept", ORDER_ID).contentType(MediaType.APPLICATION_JSON)
            .content("{\"readyBy\":\"2030-01-01T10:00:00\"}")
            .header("Authorization", bearer(FtgoRole.RESTAURANT, RestaurantMother.AJANTA_ID)))
            .andExpect(status().isOk());
    verify(orderService).accept(anyLong(), any());
  }

  @Test
  public void shouldRejectPreparingByOtherRestaurant() throws Exception {
    mockMvc.perform(post("/orders/{orderId}/preparing", ORDER_ID)
            .header("Authorization", bearer(FtgoRole.RESTAURANT, RestaurantMother.AJANTA_ID + 1)))
            .andExpect(status().isForbidden());
    verify(orderService, never()).notePreparing(anyLong());
  }

  @Test
  public void shouldRejectReadyByOtherRestaurant() throws Exception {
    mockMvc.perform(post("/orders/{orderId}/ready", ORDER_ID)
            .header("Authorization", bearer(FtgoRole.RESTAURANT, RestaurantMother.AJANTA_ID + 1)))
            .andExpect(status().isForbidden());
    verify(orderService, never()).noteReadyForPickup(anyLong());
  }

  @Test
  public void shouldRejectPickedUpByUnassignedCourier() throws Exception {
    mockMvc.perform(post("/orders/{orderId}/pickedup", ORDER_ID)
            .header("Authorization", bearer(FtgoRole.COURIER, COURIER_ID + 1)))
            .andExpect(status().isForbidden());
    verify(orderService, never()).notePickedUp(anyLong());
  }

  @Test
  public void shouldAllowPickedUpByAssignedCourier() throws Exception {
    mockMvc.perform(post("/orders/{orderId}/pickedup", ORDER_ID)
            .header("Authorization", bearer(FtgoRole.COURIER, COURIER_ID)))
            .andExpect(status().isOk());
    verify(orderService).notePickedUp(ORDER_ID);
  }

  @Test
  public void shouldAllowDeliveredByAssignedCourier() throws Exception {
    mockMvc.perform(post("/orders/{orderId}/delivered", ORDER_ID)
            .header("Authorization", bearer(FtgoRole.COURIER, COURIER_ID)))
            .andExpect(status().isOk());
    verify(orderService).noteDelivered(ORDER_ID);
  }

  @Test
  public void shouldRejectDeliveredByUnassignedCourier() throws Exception {
    mockMvc.perform(post("/orders/{orderId}/delivered", ORDER_ID)
            .header("Authorization", bearer(FtgoRole.COURIER, COURIER_ID + 1)))
            .andExpect(status().isForbidden());
    verify(orderService, never()).noteDelivered(anyLong());
  }

  @Test
  public void shouldStillAllowUnauthenticatedGet() throws Exception {
    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/orders/{orderId}", ORDER_ID))
            .andExpect(status().isOk());
  }

  private static String bearer(FtgoRole role, long id) {
    return "Bearer " + TestJwtTokens.token(role, id);
  }
}
