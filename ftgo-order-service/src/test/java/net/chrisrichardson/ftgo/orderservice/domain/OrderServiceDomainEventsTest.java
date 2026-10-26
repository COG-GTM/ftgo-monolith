package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.common.events.DomainEvent;
import net.chrisrichardson.ftgo.common.events.InProcessDomainEventBus;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.domain.events.*;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import net.chrisrichardson.ftgo.orderservice.RestaurantMother;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderServiceDomainEventsTest {

  private static final long ORDER_ID = 42L;

  private OrderRepository orderRepository;
  private RestaurantRepository restaurantRepository;
  private CourierRepository courierRepository;
  private OrderService orderService;
  private final List<DomainEvent> publishedEvents = new ArrayList<>();

  @Before
  public void setUp() {
    orderRepository = mock(OrderRepository.class);
    restaurantRepository = mock(RestaurantRepository.class);
    courierRepository = mock(CourierRepository.class);

    InProcessDomainEventBus eventBus = new InProcessDomainEventBus();
    eventBus.subscribe(DomainEvent.class, publishedEvents::add);

    orderService = new OrderService(orderRepository,
            restaurantRepository,
            Optional.empty(),
            mock(ConsumerService.class),
            courierRepository,
            new DistanceOptimizedCourierAssignmentStrategy(),
            eventBus);
  }

  @Test
  public void shouldPublishOrderCreatedEventWithGeneratedId() {
    when(restaurantRepository.findById(RestaurantMother.AJANTA_ID)).thenReturn(Optional.of(RestaurantMother.AJANTA_RESTAURANT));
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
      Order order = invocation.getArgument(0);
      order.setId(ORDER_ID);
      return order;
    });

    orderService.createOrder(OrderDetailsMother.CONSUMER_ID, RestaurantMother.AJANTA_ID,
            OrderDetailsMother.CHICKEN_VINDALOO_MENU_ITEMS_AND_QUANTITIES);

    OrderCreatedEvent event = (OrderCreatedEvent) singlePublishedEvent(OrderCreatedEvent.class);
    assertEquals(Long.valueOf(ORDER_ID), event.getOrderId());
    assertEquals(Long.valueOf(OrderDetailsMother.CONSUMER_ID), event.getConsumerId());
    assertEquals(OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL, event.getOrderTotal());
  }

  @Test
  public void shouldPublishOrderCancelledEvent() {
    givenExistingOrder();

    orderService.cancel(ORDER_ID);

    singlePublishedEvent(OrderCancelledEvent.class);
  }

  @Test
  public void shouldPublishOrderAcceptedEventAfterScheduling() {
    givenExistingOrder();
    Courier courier = new Courier(new PersonName("Ann", "Driver"),
            new Address("1 Main St", null, "Oakland", "CA", "94612"));
    courier.noteAvailable();
    when(courierRepository.findAllAvailable()).thenReturn(Collections.singletonList(courier));
    LocalDateTime readyBy = LocalDateTime.now().plusHours(1);

    orderService.accept(ORDER_ID, readyBy);

    OrderAcceptedEvent event = (OrderAcceptedEvent) singlePublishedEvent(OrderAcceptedEvent.class);
    assertEquals(readyBy, event.getReadyBy());
  }

  @Test
  public void shouldNotPublishOrderAcceptedEventWhenNoCourierIsAvailable() {
    givenExistingOrder();
    when(courierRepository.findAllAvailable()).thenReturn(Collections.emptyList());

    try {
      orderService.accept(ORDER_ID, LocalDateTime.now().plusHours(1));
      fail("expected NoCourierAvailableException");
    } catch (NoCourierAvailableException e) {
      // expected
    }

    assertTrue(publishedEvents.isEmpty());
  }

  @Test
  public void shouldPublishEventForEachFulfillmentTransition() {
    Order order = givenExistingOrder();
    order.acceptTicket(LocalDateTime.now().plusHours(1));
    order.pullDomainEvents();

    orderService.notePreparing(ORDER_ID);
    orderService.noteReadyForPickup(ORDER_ID);
    orderService.notePickedUp(ORDER_ID);
    orderService.noteDelivered(ORDER_ID);

    assertEquals(4, publishedEvents.size());
    assertTrue(publishedEvents.get(0) instanceof OrderPreparationStartedEvent);
    assertTrue(publishedEvents.get(1) instanceof OrderReadyForPickupEvent);
    assertTrue(publishedEvents.get(2) instanceof OrderPickedUpEvent);
    assertTrue(publishedEvents.get(3) instanceof OrderDeliveredEvent);
  }

  @Test
  public void shouldNotPublishEventWhenTransitionIsRejected() {
    givenExistingOrder();

    try {
      orderService.noteDelivered(ORDER_ID);
      fail("expected UnsupportedStateTransitionException");
    } catch (RuntimeException e) {
      // expected
    }

    assertTrue(publishedEvents.isEmpty());
  }

  private Order givenExistingOrder() {
    Order order = new Order(OrderDetailsMother.CONSUMER_ID, RestaurantMother.AJANTA_RESTAURANT,
            OrderDetailsMother.chickenVindalooLineItems());
    order.setId(ORDER_ID);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    return order;
  }

  private DomainEvent singlePublishedEvent(Class<? extends DomainEvent> type) {
    assertEquals(1, publishedEvents.size());
    assertTrue(type.isInstance(publishedEvents.get(0)));
    return publishedEvents.get(0);
  }
}
