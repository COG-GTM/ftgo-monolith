package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.ErrorResponse;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.RestaurantMother;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OrderControllerCreateTest {
  private MockMvc mvc;
  private RestaurantRepository restaurants;
  private ConsumerService consumers;

  @Before
  public void setUp() {
    OrderRepository orders = mock(OrderRepository.class);
    restaurants = mock(RestaurantRepository.class);
    consumers = mock(ConsumerService.class);
    when(restaurants.findById(2L)).thenReturn(Optional.of(RestaurantMother.AJANTA_RESTAURANT));
    when(orders.save(any(Order.class))).thenAnswer(invocation -> {
      Order order = invocation.getArgument(0);
      order.setId(42L);
      return order;
    });
    OrderService service = new OrderService(orders, restaurants, Optional.empty(), consumers,
            mock(CourierRepository.class), mock(CourierAssignmentStrategy.class));
    mvc = MockMvcBuilders.standaloneSetup(new OrderController(service, orders))
            .setControllerAdvice(new CreateErrorAdvice()).build();
  }

  @Test
  public void validCreateReturnsOrderId() throws Exception {
    mvc.perform(post("/orders").contentType("application/json").content(body("[{\"menuItemId\":\"1\",\"quantity\":1}]")))
            .andExpect(status().isOk()).andExpect(content().json("{\"orderId\":42}", true));
  }

  @Test
  public void rejectsMissingEmptyAndNullItems() throws Exception {
    bad("{\"consumerId\":1,\"restaurantId\":2}", "Order must contain line items");
    bad(body("null"), "Order must contain line items");
    bad(body("[]"), "Order must contain line items");
    bad(body("[null]"), "Order line items must not be null");
  }

  @Test
  public void rejectsNonPositiveQuantities() throws Exception {
    for (int quantity : new int[]{0, -1}) {
      bad(body("[{\"menuItemId\":\"1\",\"quantity\":" + quantity + "}]"), "Line item quantity must be positive");
    }
  }

  @Test
  public void rejectsInvalidMenuItems() throws Exception {
    bad(body("[{\"menuItemId\":\"secret-input\",\"quantity\":1}]"), "Invalid menu item");
    bad(body("[{\"menuItemId\":null,\"quantity\":1}]"), "Invalid menu item");
  }

  @Test
  public void rejectsUnknownConsumer() throws Exception {
    doThrow(new ConsumerNotFoundException()).when(consumers).validateOrderForConsumer(anyLong(), any());
    bad(body("[{\"menuItemId\":\"1\",\"quantity\":1}]"), "Consumer not found");
  }

  @Test
  public void rejectsNullConsumerId() throws Exception {
    bad(body("[]").replace("\"consumerId\":1", "\"consumerId\":null"), "Consumer ID must be positive");
  }

  @Test
  public void unknownRestaurantRemainsNotFound() throws Exception {
    when(restaurants.findById(2L)).thenReturn(Optional.empty());
    mvc.perform(post("/orders").contentType("application/json").content(body("[{\"menuItemId\":\"1\",\"quantity\":1}]")))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found")).andExpect(jsonPath("$.path").value("/orders"));
  }

  private String body(String items) {
    return "{\"consumerId\":1,\"restaurantId\":2,\"lineItems\":" + items + "}";
  }

  private void bad(String body, String message) throws Exception {
    mvc.perform(post("/orders").contentType("application/json").content(body))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request")).andExpect(jsonPath("$.message").value(message))
            .andExpect(jsonPath("$.path").value("/orders")).andExpect(jsonPath("$.timestamp").exists());
  }

  // The application advice is in a downstream module; mirror its error contract here.
  @RestControllerAdvice
  public static class CreateErrorAdvice {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException e, HttpServletRequest request) {
      return ResponseEntity.badRequest().body(new ErrorResponse(400, "Bad Request", e.getMessage(), request.getRequestURI(), null));
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(RestaurantNotFoundException e, HttpServletRequest request) {
      return ResponseEntity.status(404).body(new ErrorResponse(404, "Not Found", e.getMessage(), request.getRequestURI(), null));
    }
  }
}
