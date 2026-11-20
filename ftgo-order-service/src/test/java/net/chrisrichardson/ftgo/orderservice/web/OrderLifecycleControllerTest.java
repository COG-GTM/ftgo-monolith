package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.chickenVindalooLineItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderLifecycleControllerTest {
  private Order order;
  private MockMvc mvc;

  @Before
  public void setUp() {
    OrderRepository orders = mock(OrderRepository.class);
    CourierRepository couriers = mock(CourierRepository.class);
    Courier courier = new Courier(new PersonName("Test", "Courier"),
            new Address("1 Main Street", null, "Oakland", "CA", "94619"));
    when(couriers.findAllAvailable()).thenReturn(Collections.singletonList(courier));
    order = new Order(1L, new Restaurant(2L, "Test Restaurant", new RestaurantMenu(Collections.emptyList())),
            chickenVindalooLineItems());
    order.setId(42L);
    when(orders.findById(42L)).thenReturn(Optional.of(order));
    when(orders.findById(999999L)).thenReturn(Optional.empty());

    OrderService service = new OrderService(orders, null, Optional.empty(), null, couriers,
            (available, orderToAssign) -> available.get(0));
    ObjectMapper mapper = new ObjectMapper().registerModule(new MoneyModule()).registerModule(new JavaTimeModule());
    mvc = MockMvcBuilders.standaloneSetup(new OrderController(service, orders))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
  }

  @Test
  public void shouldReturnUpdatedOrderFromEachLifecyclePost() throws Exception {
    assertUpdatedOrder(jsonPost("/orders/42/accept", acceptance()), "ACCEPTED");
    assertUpdatedOrder(post("/orders/42/preparing"), "PREPARING");
    assertUpdatedOrder(post("/orders/42/ready"), "READY_FOR_PICKUP");
    assertUpdatedOrder(post("/orders/42/pickedup"), "PICKED_UP");
    assertUpdatedOrder(post("/orders/42/delivered"), "DELIVERED");
  }

  @Test
  public void shouldSignalUnknownOrders() throws Exception {
    assertFailure(jsonPost("/orders/999999/accept", acceptance()), OrderNotFoundException.class);
    for (String transition : new String[]{"preparing", "ready", "pickedup", "delivered"}) {
      assertFailure(post("/orders/999999/" + transition), OrderNotFoundException.class);
    }
    mvc.perform(jsonPost("/orders/999999/revise", "{\"revisedLineItemQuantities\":{\"1\":0}}"))
            .andExpect(status().isNotFound());
    mvc.perform(post("/orders/999999/cancel")).andExpect(status().isNotFound());
  }

  @Test
  public void shouldSignalOutOfOrderTransition() throws Exception {
    assertFailure(post("/orders/42/delivered"), UnsupportedStateTransitionException.class);
    assertEquals(OrderState.APPROVED, order.getOrderState());
  }

  @Test
  public void shouldRejectAcceptWithoutReadyBy() throws Exception {
    assertFailure(post("/orders/42/accept"), IllegalArgumentException.class);
    assertFailure(jsonPost("/orders/42/accept", "{}"), IllegalArgumentException.class);
    assertEquals(OrderState.APPROVED, order.getOrderState());
  }

  @Test
  public void shouldRejectNullEmptyOrNegativeRevisions() throws Exception {
    for (String body : new String[]{"{}", "{\"revisedLineItemQuantities\":{}}",
            "{\"revisedLineItemQuantities\":{\"1\":-1}}", "{\"revisedLineItemQuantities\":{\"1\":null}}"}) {
      assertFailure(jsonPost("/orders/42/revise", body), IllegalArgumentException.class);
    }
    assertEquals(chickenVindalooLineItems().get(0).getQuantity(), order.getLineItems().get(0).getQuantity());
  }

  private void assertUpdatedOrder(MockHttpServletRequestBuilder request, String state) throws Exception {
    String orderJson = mvc.perform(request)
            .andExpect(status().isOk()).andExpect(jsonPath("$.orderId").value(42))
            .andExpect(jsonPath("$.state").value(state)).andReturn().getResponse().getContentAsString();
    mvc.perform(get("/orders/42")).andExpect(content().json(orderJson, true));
  }

  private void assertFailure(MockHttpServletRequestBuilder request, Class<? extends Exception> expected) throws Exception {
    try {
      mvc.perform(request);
      fail("Expected " + expected.getSimpleName());
    } catch (NestedServletException e) {
      assertThat(e.getCause(), instanceOf(expected));
    }
  }

  private MockHttpServletRequestBuilder jsonPost(String path, String body) {
    return post(path).contentType(MediaType.APPLICATION_JSON).content(body);
  }

  private String acceptance() {
    return "{\"readyBy\":\"" + LocalDateTime.now().plusHours(1) + "\"}";
  }
}
