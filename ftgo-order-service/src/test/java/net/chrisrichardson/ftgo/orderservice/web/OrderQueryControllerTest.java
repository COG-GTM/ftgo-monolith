package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import java.util.Arrays;
import java.util.Collections;

import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.chickenVindalooLineItems;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderQueryControllerTest {
  private OrderRepository orderRepository;
  private MockMvc mvc;

  @Before
  public void setUp() {
    orderRepository = mock(OrderRepository.class);
    ObjectMapper mapper = new ObjectMapper().registerModule(new MoneyModule());
    mvc = MockMvcBuilders.standaloneSetup(new OrderController(mock(OrderService.class), orderRepository))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
            .build();
  }

  @Test
  public void keepsLegacyLookupOrderAndResponseShape() throws Exception {
    Restaurant restaurant = CHICKEN_VINDALOO_ORDER.getRestaurant();
    Order first = new Order(-7L, restaurant, chickenVindalooLineItems());
    first.setId(42L);
    Order second = new Order(-7L, restaurant, chickenVindalooLineItems());
    second.setId(15L);
    second.cancel();
    when(orderRepository.findAllByConsumerId(-7L)).thenReturn(Arrays.asList(first, second));

    mvc.perform(get("/orders").param("consumerId", " -0x7 ", "invalid"))
            .andExpect(status().isOk())
            .andExpect(content().json("[" + expectedOrder(42, "APPROVED") + ","
                    + expectedOrder(15, "CANCELLED") + "]", true));
    verify(orderRepository).findAllByConsumerId(-7L);
    verifyNoMoreInteractions(orderRepository);
  }

  @Test
  public void filtersByStateWithoutPaging() throws Exception {
    when(orderRepository.findAllByConsumerIdAndOrderState(11L, OrderState.APPROVED))
            .thenReturn(Collections.singletonList(CHICKEN_VINDALOO_ORDER));

    mvc.perform(get("/orders").param("consumerId", "11").param("state", "APPROVED"))
            .andExpect(status().isOk())
            .andExpect(content().json("[" + expectedOrder(CHICKEN_VINDALOO_ORDER.getId(), "APPROVED") + "]", true));
    verify(orderRepository).findAllByConsumerIdAndOrderState(11L, OrderState.APPROVED);
    verifyNoMoreInteractions(orderRepository);
  }

  @Test
  public void pagesByIdWithDefaultSize() throws Exception {
    mvc.perform(get("/orders").param("consumerId", "12").param("page", "2"))
            .andExpect(status().isOk()).andExpect(content().json("[]", true));
    verify(orderRepository).findAllByConsumerId(12L, PageRequest.of(2, 20, Sort.Direction.ASC, "id"));
    verifyNoMoreInteractions(orderRepository);
  }

  @Test
  public void combinesStateAndPagingWithDefaultPage() throws Exception {
    mvc.perform(get("/orders").param("consumerId", "13").param("state", "PICKED_UP").param("size", "100"))
            .andExpect(status().isOk()).andExpect(content().json("[]", true));
    verify(orderRepository).findAllByConsumerIdAndOrderState(
            13L, OrderState.PICKED_UP, PageRequest.of(0, 100, Sort.Direction.ASC, "id"));
    verifyNoMoreInteractions(orderRepository);
  }

  @Test
  public void skipsJpaForOffsetsBeyondIntegerRange() throws Exception {
    for (String page : Arrays.asList("2147483647", Long.toString(Long.MAX_VALUE))) {
      mvc.perform(get("/orders").param("consumerId", "14").param("page", page).param("size", "2"))
              .andExpect(status().isOk()).andExpect(content().json("[]", true));
    }
    verifyZeroInteractions(orderRepository);
  }

  @Test
  public void rejectsInvalidConsumerIdsBeforeQuerying() throws Exception {
    expectInvalid(get("/orders"));
    for (String value : Arrays.asList("", " ", "invalid", "1,2", "9223372036854775808")) {
      expectInvalid(get("/orders").param("consumerId", value));
    }
    verifyZeroInteractions(orderRepository);
  }

  @Test
  public void rejectsInvalidModifiersBeforeQuerying() throws Exception {
    for (String value : Arrays.asList("", "approved", "UNKNOWN")) {
      expectInvalid(get("/orders").param("consumerId", "14").param("state", value));
    }
    for (String value : Arrays.asList("", "-1", "invalid", "9223372036854775808")) {
      expectInvalid(get("/orders").param("consumerId", "14").param("page", value));
    }
    for (String value : Arrays.asList("", "-1", "0", "101", "invalid", "2147483648")) {
      expectInvalid(get("/orders").param("consumerId", "14").param("size", value));
    }
    verifyZeroInteractions(orderRepository);
  }

  private void expectInvalid(MockHttpServletRequestBuilder request) throws Exception {
    try {
      mvc.perform(request);
      fail("Expected IllegalArgumentException");
    } catch (NestedServletException ex) {
      assertTrue(ex.getCause().toString(), ex.getCause() instanceof IllegalArgumentException);
    }
  }

  private String expectedOrder(long id, String state) {
    return "{\"orderId\":" + id + ",\"state\":\"" + state + "\",\"orderTotal\":\""
            + CHICKEN_VINDALOO_ORDER_TOTAL.asString() + "\",\"restaurantName\":\"\","
            + "\"assignedCourier\":null,\"courierActions\":null,\"estimatedDeliveryTime\":null}";
  }
}
