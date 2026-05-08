package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.CourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.RestaurantMother;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderServiceTest {

  private OrderRepository orderRepository;
  private RestaurantServiceClient restaurantServiceClient;
  private ConsumerService consumerService;
  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;
  private OrderService orderService;

  @Before
  public void setUp() {
    orderRepository = mock(OrderRepository.class);
    restaurantServiceClient = mock(RestaurantServiceClient.class);
    consumerService = mock(ConsumerService.class);
    courierRepository = mock(CourierRepository.class);
    courierAssignmentStrategy = mock(CourierAssignmentStrategy.class);

    orderService = new OrderService(orderRepository,
            restaurantServiceClient,
            Optional.empty(),
            consumerService,
            courierRepository,
            courierAssignmentStrategy);
  }

  @Test
  public void shouldCreateOrderUsingRestaurantServiceClient() {
    when(restaurantServiceClient.findById(RestaurantMother.AJANTA_ID))
            .thenReturn(RestaurantMother.AJANTA_RESTAURANT);

    Order order = orderService.createOrder(OrderDetailsMother.CONSUMER_ID,
            RestaurantMother.AJANTA_ID,
            OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);

    assertNotNull(order);
    assertEquals(RestaurantMother.AJANTA_ID, (long) order.getRestaurantId());
    assertEquals(RestaurantMother.AJANTA_RESTAURANT_NAME, order.getRestaurantName());

    verify(restaurantServiceClient).findById(RestaurantMother.AJANTA_ID);
    verify(consumerService).validateOrderForConsumer(eq(OrderDetailsMother.CONSUMER_ID), any());
    verify(orderRepository).save(order);
  }

  @Test(expected = RestaurantNotFoundException.class)
  public void shouldThrowWhenRestaurantNotFound() {
    when(restaurantServiceClient.findById(RestaurantMother.AJANTA_ID)).thenReturn(null);

    orderService.createOrder(OrderDetailsMother.CONSUMER_ID,
            RestaurantMother.AJANTA_ID,
            OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);
  }
}
