package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.CourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.orderservice.restaurant.RestaurantClient;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CONSUMER_ID;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_ID;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The order service reads restaurants through the {@link RestaurantClient} anti-corruption layer,
 * so its behavior is the same whether the restaurant data comes from the shared database or from
 * the extracted restaurant service.
 */
public class OrderServiceRestaurantIntegrationTest {

  private RestaurantClient restaurantClient;
  private OrderRepository orderRepository;
  private ConsumerService consumerService;
  private OrderService orderService;

  @Before
  public void setUp() {
    restaurantClient = mock(RestaurantClient.class);
    orderRepository = mock(OrderRepository.class);
    consumerService = mock(ConsumerService.class);
    orderService = new OrderService(orderRepository, restaurantClient, Optional.empty(), consumerService,
            mock(CourierRepository.class), mock(CourierAssignmentStrategy.class));
  }

  @Test
  public void shouldPriceOrderLineItemsFromTheRestaurantMenu() {
    when(restaurantClient.findRestaurant(AJANTA_ID)).thenReturn(Optional.of(AJANTA_RESTAURANT));

    Order order = orderService.createOrder(CONSUMER_ID, AJANTA_ID, CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);

    assertEquals(CHICKEN_VINDALOO_ORDER_TOTAL, order.getOrderTotal());
    verify(consumerService).validateOrderForConsumer(CONSUMER_ID, CHICKEN_VINDALOO_ORDER_TOTAL);
    verify(orderRepository).save(order);
  }

  @Test
  public void shouldRejectOrderForUnknownRestaurant() {
    when(restaurantClient.findRestaurant(AJANTA_ID)).thenReturn(Optional.empty());

    try {
      orderService.createOrder(CONSUMER_ID, AJANTA_ID, CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);
      fail("Expected RestaurantNotFoundException");
    } catch (RestaurantNotFoundException expected) {
      // expected
    }
  }
}
