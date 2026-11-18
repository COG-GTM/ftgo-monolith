package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.Optional;

import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OrderValidationTest {
  private OrderRepository orders;
  private RestaurantRepository restaurants;
  private ConsumerService consumers;
  private OrderService service;
  private Order order;
  private MockMvc mvc;

  @Before
  public void setUp() {
    orders = mock(OrderRepository.class);
    restaurants = mock(RestaurantRepository.class);
    consumers = mock(ConsumerService.class);
    when(restaurants.findById(1L)).thenReturn(Optional.of(AJANTA_RESTAURANT));
    when(restaurants.findById(99L)).thenReturn(Optional.empty());
    order = new Order(1L, AJANTA_RESTAURANT, Collections.singletonList(
            new OrderLineItem("1", "Chicken", CHICKEN_VINDALOO_PRICE, 5)));
    order.setId(1L);
    when(orders.findById(1L)).thenReturn(Optional.of(order));
    when(orders.findById(99L)).thenReturn(Optional.empty());
    doAnswer(invocation -> {
      ((Order) invocation.getArguments()[0]).setId(2L);
      return invocation.getArguments()[0];
    }).when(orders).save(any(Order.class));
    service = new OrderService(orders, restaurants, Optional.empty(), consumers,
            mock(CourierRepository.class), mock(CourierAssignmentStrategy.class));
    mvc = MockMvcBuilders.standaloneSetup(new OrderController(service, orders))
            .setControllerAdvice(new MappingAdvice())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(
                    new ObjectMapper().registerModule(new MoneyModule())))
            .build();
  }

  @Test
  public void invalidCreateBodiesDoNotPersist() throws Exception {
    String[] bodies = {"{}", "{\"consumerId\":null}",
            create("null", "1", "[{\"menuItemId\":\"1\",\"quantity\":1}]"),
            create("1", "null", "[{\"menuItemId\":\"1\",\"quantity\":1}]"),
            create("0", "1", "[]"), create("-1", "1", "[]"),
            create("1", "0", "[]"), create("1", "-1", "[]"),
            create("1", "1", "null"), create("1", "1", "[]"),
            create("1", "1", "[null]")};
    for (String body : bodies) {
      check("/orders", body, 400);
    }
    for (String id : new String[]{"null", "\"\"", "\"  \""}) {
      check("/orders", create("1", "1", "[{\"menuItemId\":" + id + ",\"quantity\":1}]"), 400);
    }
    for (int quantity : new int[]{0, -1}) {
      check("/orders", create("1", "1", "[{\"menuItemId\":\"1\",\"quantity\":" + quantity + "}]"), 400);
    }
    verify(orders, never()).save(any(Order.class));
  }

  @Test
  public void unknownCreateReferences() throws Exception {
    check("/orders", create("1", "99", items("1")), 404);
    check("/orders", create("1", "1", items("missing")), 400);
    doThrow(new ConsumerNotFoundException()).when(consumers).validateOrderForConsumer(anyLong(), any(Money.class));
    check("/orders", create("99", "1", items("1")), 400);
    verify(orders, never()).save(any(Order.class));
  }

  @Test
  public void validCreateKeepsResponse() throws Exception {
    mvc.perform(post("/orders").contentType("application/json")
            .content(create("1", "1", items("1"))))
            .andExpect(status().isOk()).andExpect(content().json("{\"orderId\":2}"));
    verify(orders).save(any(Order.class));
  }

  @Test
  public void invalidRevisionsDoNotMutate() throws Exception {
    for (String body : new String[]{"{}", "{\"revisedLineItemQuantities\":null}",
            revision("{}"), revision("{\"1\":null}"), revision("{\"1\":-1}"),
            revision("{\"missing\":1}")}) {
      check("/orders/1/revise", body, 400);
    }
    assertEquals(5, order.getLineItems().get(0).getQuantity());
  }

  @Test
  public void minimumExceptionIsBadRequestWithoutMutation() throws Exception {
    ReflectionTestUtils.setField(order, "orderMinimum", Money.ZERO);
    check("/orders/1/revise", revision("{\"1\":1}"), 400);
    assertEquals(5, order.getLineItems().get(0).getQuantity());
  }

  @Test
  public void reviseUnknownOrderAndInvalidState() throws Exception {
    check("/orders/99/revise", revision("{\"1\":1}"), 404);
    order.cancel();
    check("/orders/1/revise", revision("{\"1\":1}"), 409);
  }

  @Test
  public void validAndZeroRevisionsKeepResponse() throws Exception {
    mvc.perform(post("/orders/1/revise").contentType("application/json").content(revision("{\"1\":10}")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.orderTotal").value("123.40"));
    check("/orders/1/revise", revision("{\"1\":0}"), 200);
    assertEquals("0.00", order.getOrderTotal().asString());
  }

  @Test
  public void cancelBehaviorIsUnchanged() throws Exception {
    check("/orders/99/cancel", "{}", 404);
    check("/orders/1/cancel", "{}", 200);
    check("/orders/1/cancel", "{}", 409);
  }

  @Test
  public void serviceAlsoRejectsMissingInput() {
    try {
      service.createOrder(1, 1, null);
      fail("Expected invalid create");
    } catch (IllegalArgumentException expected) {
      verify(orders, never()).save(any(Order.class));
    }
    try {
      service.reviseOrder(1, new OrderRevision(Optional.empty(), Collections.singletonMap("1", null)));
      fail("Expected invalid revision");
    } catch (IllegalArgumentException expected) {
      assertEquals(5, order.getLineItems().get(0).getQuantity());
    }
  }

  private String items(String id) {
    return "[{\"menuItemId\":\"" + id + "\",\"quantity\":1}]";
  }

  private String create(String consumer, String restaurant, String items) {
    return "{\"consumerId\":" + consumer + ",\"restaurantId\":" + restaurant + ",\"lineItems\":" + items + "}";
  }

  private String revision(String quantities) {
    return "{\"revisedLineItemQuantities\":" + quantities + "}";
  }

  private void check(String path, String body, int status) throws Exception {
    mvc.perform(post(path).contentType("application/json").content(body)).andExpect(status().is(status));
  }

  @ControllerAdvice
  public static class MappingAdvice {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> badRequest() { return ResponseEntity.badRequest().build(); }

    @ExceptionHandler({OrderNotFoundException.class, RestaurantNotFoundException.class})
    public ResponseEntity<Void> notFound() { return ResponseEntity.notFound().build(); }

    @ExceptionHandler(UnsupportedStateTransitionException.class)
    public ResponseEntity<Void> conflict() { return ResponseEntity.status(409).build(); }
  }
}
