package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OrderQueryControllerTest {
  private OrderRepository repository;
  private MockMvc mvc;

  @Before
  public void setUp() {
    repository = mock(OrderRepository.class);
    ObjectMapper mapper = new ObjectMapper().registerModule(new MoneyModule()).registerModule(new JavaTimeModule());
    mvc = MockMvcBuilders.standaloneSetup(new OrderController(mock(OrderService.class), repository))
            .setControllerAdvice(new InvalidQueryAdvice())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
    Order approved = order(1L, OrderState.APPROVED);
    Order cancelled = order(2L, OrderState.CANCELLED);
    when(repository.findAllByConsumerId(1L)).thenReturn(Arrays.asList(approved, cancelled));
  }

  private Order order(long id, OrderState state) {
    Order order = mock(Order.class);
    Restaurant restaurant = mock(Restaurant.class);
    when(restaurant.getName()).thenReturn("Restaurant");
    when(order.getRestaurant()).thenReturn(restaurant);
    when(order.getId()).thenReturn(id);
    when(order.getConsumerId()).thenReturn(1L);
    when(order.getOrderState()).thenReturn(state);
    when(order.getOrderTotal()).thenReturn(new Money("10.00"));
    when(order.getLineItems()).thenReturn(Collections.singletonList(new OrderLineItem("item", "Meal", new Money("5.00"), 2)));
    return order;
  }

  @Test
  public void rejectsMissingEmptyAndNonNumericConsumerId() throws Exception {
    for (String query : Arrays.asList("", "?consumerId=", "?consumerId=abc", "?consumerId=9223372036854775808")) {
      mvc.perform(get("/orders" + query)).andExpect(status().isBadRequest());
    }
    verify(repository, never()).findAllByConsumerId(anyLong());
  }

  @Test
  public void rejectsInvalidStateAndPaging() throws Exception {
    for (String query : Arrays.asList("state=BOGUS", "state=", "state=approved", "page=-1", "page=abc", "page=", "page=2147483648",
            "size=0", "size=-1", "size=101", "size=abc", "size=", "size=2147483648")) {
      mvc.perform(get("/orders?consumerId=1&" + query)).andExpect(status().isBadRequest());
    }
    verify(repository, never()).findAllByConsumerId(anyLong());
  }

  @Test
  public void returnsPlainArrayWithExistingAndNewFields() throws Exception {
    mvc.perform(get("/orders?consumerId=1")).andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].orderId", is(1)))
            .andExpect(jsonPath("$[0].state", is("APPROVED")))
            .andExpect(jsonPath("$[0].orderTotal", is("10.00")))
            .andExpect(jsonPath("$[0].restaurantName", is("Restaurant")))
            .andExpect(jsonPath("$[0]", hasKey("assignedCourier")))
            .andExpect(jsonPath("$[0]", hasKey("courierActions")))
            .andExpect(jsonPath("$[0]", hasKey("estimatedDeliveryTime")))
            .andExpect(jsonPath("$[0].consumerId", is(1)))
            .andExpect(jsonPath("$[0].lineItems[0].menuItemId", is("item")))
            .andExpect(jsonPath("$[0].lineItems[0].name", is("Meal")))
            .andExpect(jsonPath("$[0].lineItems[0].price", is("5.00")))
            .andExpect(jsonPath("$[0].lineItems[0].quantity", is(2)));
  }

  @Test
  public void filtersBeforePaging() throws Exception {
    mvc.perform(get("/orders?consumerId=1&state=APPROVED&page=0&size=1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].state", is("APPROVED")));
    mvc.perform(get("/orders?consumerId=1&state=CANCELLED&page=0&size=1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].orderId", is(2)));
    mvc.perform(get("/orders?consumerId=1&state=ACCEPTED"))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  public void pagesAndDefaultsRemainArrays() throws Exception {
    mvc.perform(get("/orders?consumerId=1&page=0&size=1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    mvc.perform(get("/orders?consumerId=1&page=1&size=1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].orderId", is(2)));
    mvc.perform(get("/orders?consumerId=1&size=1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].orderId", is(1)));
    mvc.perform(get("/orders?consumerId=1&page=0"))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
    mvc.perform(get("/orders?consumerId=1&page=2147483647&size=100"))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  public void unpagedResultsAreNotLimitedAndDefaultPageSizeIsTwenty() throws Exception {
    Order order = order(1L, OrderState.APPROVED);
    when(repository.findAllByConsumerId(1L)).thenReturn(Collections.nCopies(25, order));
    mvc.perform(get("/orders?consumerId=1")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(25)));
    mvc.perform(get("/orders?consumerId=1&page=0")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(20)));
    mvc.perform(get("/orders?consumerId=1&page=1")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(5)));
  }

  @Test
  public void emptyOrdersAndLineItemsRemainArrays() throws Exception {
    when(repository.findAllByConsumerId(1L)).thenReturn(Collections.emptyList());
    mvc.perform(get("/orders?consumerId=1")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    Order order = order(1L, OrderState.APPROVED);
    when(order.getLineItems()).thenReturn(Collections.emptyList());
    when(repository.findById(1L)).thenReturn(Optional.of(order));
    mvc.perform(get("/orders/1")).andExpect(status().isOk()).andExpect(jsonPath("$.lineItems", hasSize(0)));
  }

  @Test
  public void singleOrderIncludesNewFieldsAndUnknownIsNotFound() throws Exception {
    Order order = order(1L, OrderState.APPROVED);
    when(repository.findById(1L)).thenReturn(Optional.of(order));
    when(repository.findById(999L)).thenReturn(Optional.empty());
    mvc.perform(get("/orders/1")).andExpect(status().isOk())
            .andExpect(jsonPath("$.consumerId", is(1)))
            .andExpect(jsonPath("$.lineItems[0].menuItemId", is("item")))
            .andExpect(jsonPath("$.lineItems[0].name", is("Meal")))
            .andExpect(jsonPath("$.lineItems[0].price", is("5.00")))
            .andExpect(jsonPath("$.lineItems[0].quantity", is(2)));
    mvc.perform(get("/orders/999")).andExpect(status().isNotFound());
  }

  @ControllerAdvice
  public static class InvalidQueryAdvice {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void invalidQuery() {
    }
  }
}
