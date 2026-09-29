package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.consumerservice.api.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerVerificationService;
import net.chrisrichardson.ftgo.domain.CourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CONSUMER_ID;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_ID;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderServiceTest {

  private OrderRepository orderRepository;
  private RestaurantRepository restaurantRepository;
  private ConsumerVerificationService consumerVerificationService;
  private OrderService orderService;

  @Before
  public void setUp() {
    orderRepository = mock(OrderRepository.class);
    restaurantRepository = mock(RestaurantRepository.class);
    consumerVerificationService = mock(ConsumerVerificationService.class);
    orderService = new OrderService(orderRepository,
            restaurantRepository,
            Optional.empty(),
            consumerVerificationService,
            mock(CourierRepository.class),
            mock(CourierAssignmentStrategy.class));

    when(restaurantRepository.findById(AJANTA_ID)).thenReturn(Optional.of(AJANTA_RESTAURANT));
  }

  @Test
  public void shouldValidateConsumerAndSaveOrder() {
    Order order = orderService.createOrder(CONSUMER_ID, AJANTA_ID, CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);

    assertEquals(CHICKEN_VINDALOO_ORDER_TOTAL, order.getOrderTotal());
    verify(consumerVerificationService).validateOrderForConsumer(CONSUMER_ID, CHICKEN_VINDALOO_ORDER_TOTAL);
    verify(orderRepository).save(order);
  }

  @Test(expected = ConsumerNotFoundException.class)
  public void shouldNotSaveOrderWhenConsumerVerificationFails() {
    doThrow(new ConsumerNotFoundException())
            .when(consumerVerificationService).validateOrderForConsumer(CONSUMER_ID, CHICKEN_VINDALOO_ORDER_TOTAL);

    try {
      orderService.createOrder(CONSUMER_ID, AJANTA_ID, CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);
    } finally {
      verify(orderRepository, never()).save(any(Order.class));
    }
  }
}
