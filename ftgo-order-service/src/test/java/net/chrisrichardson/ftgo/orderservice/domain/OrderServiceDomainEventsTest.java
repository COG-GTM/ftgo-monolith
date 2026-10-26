package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.events.DomainEvent;
import net.chrisrichardson.ftgo.common.events.DomainEventPublisher;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.domain.events.OrderCancelledEvent;
import net.chrisrichardson.ftgo.domain.events.OrderCreatedEvent;
import net.chrisrichardson.ftgo.domain.events.OrderPreparingEvent;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.RestaurantMother;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderServiceDomainEventsTest {

  private static final long ORDER_ID = 101L;

  private OrderRepository orderRepository;
  private RestaurantRepository restaurantRepository;
  private DomainEventPublisher domainEventPublisher;
  private OrderService orderService;

  @Before
  public void setUp() {
    orderRepository = mock(OrderRepository.class);
    restaurantRepository = mock(RestaurantRepository.class);
    domainEventPublisher = mock(DomainEventPublisher.class);
    orderService = new OrderService(orderRepository,
            restaurantRepository,
            Optional.empty(),
            mock(ConsumerService.class),
            mock(CourierRepository.class),
            mock(CourierAssignmentStrategy.class),
            domainEventPublisher);
  }

  @Test
  public void createOrderShouldPublishOrderCreatedEventWithAssignedId() {
    when(restaurantRepository.findById(RestaurantMother.AJANTA_ID)).thenReturn(Optional.of(RestaurantMother.AJANTA_RESTAURANT));
    doAnswer(invocation -> {
      invocation.<Order>getArgument(0).setId(ORDER_ID);
      return invocation.getArgument(0);
    }).when(orderRepository).save(any(Order.class));

    orderService.createOrder(OrderDetailsMother.CONSUMER_ID, RestaurantMother.AJANTA_ID,
            OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);

    OrderCreatedEvent event = (OrderCreatedEvent) singlePublishedEvent();
    assertEquals(OrderDetailsMother.CONSUMER_ID, event.getConsumerId());
    assertEquals(OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL, event.getOrderTotal());
  }

  @Test
  public void cancelShouldPublishOrderCancelledEvent() {
    Order order = existingOrder();

    orderService.cancel(ORDER_ID);

    assertTrue(singlePublishedEvent() instanceof OrderCancelledEvent);
    assertEquals(OrderState.CANCELLED, order.getOrderState());
  }

  @Test
  public void notePreparingShouldPublishOrderPreparingEvent() {
    Order order = existingOrder();
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.releaseDomainEvents();

    orderService.notePreparing(ORDER_ID);

    assertTrue(singlePublishedEvent() instanceof OrderPreparingEvent);
  }

  private Order existingOrder() {
    Order order = new Order(OrderDetailsMother.CONSUMER_ID, RestaurantMother.AJANTA_RESTAURANT,
            OrderDetailsMother.chickenVindalooLineItems());
    order.setId(ORDER_ID);
    order.releaseDomainEvents();
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    return order;
  }

  @SuppressWarnings("unchecked")
  private DomainEvent singlePublishedEvent() {
    ArgumentCaptor<List<? extends DomainEvent>> captor = ArgumentCaptor.forClass((Class) List.class);
    verify(domainEventPublisher).publish(eq(Order.class), eq(ORDER_ID), captor.capture());
    List<? extends DomainEvent> events = captor.getValue();
    assertEquals(1, events.size());
    return events.get(0);
  }
}
