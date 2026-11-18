package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.UnsupportedStateTransitionException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.DistanceOptimizedCourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.ORDER_ID;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_ID;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderLifecycleControllerTest {

  private static final long COURIER_ID = 7L;
  private final OrderRepository orderRepository = mock(OrderRepository.class);
  private final CourierRepository courierRepository = mock(CourierRepository.class);
  private MockMvc mvc;

  @Before
  public void setUp() {
    RestaurantRepository restaurantRepository = mock(RestaurantRepository.class);
    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.of(AJANTA_RESTAURANT));
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      Order order = (Order) invocation.getArguments()[0];
      order.setId(ORDER_ID);
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
      return order;
    });

    OrderService orderService = new OrderService(orderRepository, restaurantRepository,
            Optional.empty(), mock(ConsumerService.class), courierRepository,
            new DistanceOptimizedCourierAssignmentStrategy());
    ObjectMapper mapper = new ObjectMapper()
            .registerModule(new MoneyModule())
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService, orderRepository))
            .setControllerAdvice(new OrderErrorAdvice())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
            .build();
  }

  @Test
  public void shouldReturnUpdatedOrderAtEachLifecycleStep() throws Exception {
    createOrder();
    Courier courier = new Courier();
    ReflectionTestUtils.setField(courier, "id", COURIER_ID);
    courier.noteAvailable();
    when(courierRepository.findAllAvailable()).thenReturn(Collections.singletonList(courier));

    LocalDateTime readyBy = LocalDateTime.now().plusHours(1).withNano(0);
    assertUpdatedOrder(accept(readyBy), "ACCEPTED");
    assertUpdatedOrder(post("/orders/{id}/preparing", ORDER_ID), "PREPARING");
    assertUpdatedOrder(post("/orders/{id}/ready", ORDER_ID), "READY_FOR_PICKUP");
    assertUpdatedOrder(post("/orders/{id}/pickedup", ORDER_ID), "PICKED_UP");
    assertUpdatedOrder(post("/orders/{id}/delivered", ORDER_ID), "DELIVERED");
  }

  @Test
  public void shouldRejectMissingReadyBy() throws Exception {
    createOrder();
    mvc.perform(post("/orders/{id}/accept", ORDER_ID).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertEquals("readyBy is required", result.getResolvedException().getMessage()));
    assertApprovedOrder();
  }

  @Test
  public void shouldRejectPastReadyBy() throws Exception {
    createOrder();
    mvc.perform(accept(LocalDateTime.now().minusHours(1)))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertEquals("readyBy is not in the future", result.getResolvedException().getMessage()));
    assertApprovedOrder();
  }

  @Test
  public void shouldRejectPreparingBeforeAcceptance() throws Exception {
    createOrder();
    mvc.perform(post("/orders/{id}/preparing", ORDER_ID))
            .andExpect(status().isConflict());
    assertApprovedOrder();
  }

  @Test
  public void shouldRejectUnknownOrder() throws Exception {
    mvc.perform(accept(LocalDateTime.now().plusHours(1)))
            .andExpect(status().isNotFound());
  }

  private void createOrder() throws Exception {
    mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"consumerId\":1,\"restaurantId\":1,\"lineItems\":[{\"menuItemId\":\"1\",\"quantity\":5}]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value((int) ORDER_ID));
  }

  private MockHttpServletRequestBuilder accept(LocalDateTime readyBy) {
    return post("/orders/{id}/accept", ORDER_ID).contentType(MediaType.APPLICATION_JSON)
            .content("{\"readyBy\":\"" + readyBy + "\"}");
  }

  private void assertUpdatedOrder(MockHttpServletRequestBuilder request, String state) throws Exception {
    String response = mvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.state").value(state))
            .andExpect(jsonPath("$.orderId").value((int) ORDER_ID))
            .andExpect(jsonPath("$.orderTotal").value(CHICKEN_VINDALOO_ORDER_TOTAL.asString()))
            .andExpect(jsonPath("$.restaurantName").value(AJANTA_RESTAURANT_NAME))
            .andExpect(jsonPath("$.assignedCourier").value((int) COURIER_ID))
            .andExpect(jsonPath("$.courierActions[0].type").value("PICKUP"))
            .andExpect(jsonPath("$.courierActions[1].type").value("DROPOFF"))
            .andExpect(jsonPath("$.estimatedDeliveryTime").isString())
            .andReturn().getResponse().getContentAsString();

    mvc.perform(get("/orders/{id}", ORDER_ID))
            .andExpect(status().isOk())
            .andExpect(content().json(response, true));
  }

  private void assertApprovedOrder() throws Exception {
    mvc.perform(get("/orders/{id}", ORDER_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("APPROVED"));
  }

  // The application module owns GlobalExceptionHandler and depends on this module.
  @ControllerAdvice
  public static class OrderErrorAdvice {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void badRequest() {
    }

    @ExceptionHandler(UnsupportedStateTransitionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public void conflict() {
    }

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {
    }
  }
}
