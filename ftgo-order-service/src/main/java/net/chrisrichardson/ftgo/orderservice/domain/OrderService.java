package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.consumerservice.api.web.ConsumerServiceClient;
import net.chrisrichardson.ftgo.courierservice.api.CourierServiceClient;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import net.chrisrichardson.ftgo.restaurantservice.api.web.GetRestaurantResponse;
import net.chrisrichardson.ftgo.restaurantservice.api.web.RestaurantServiceClient;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Transactional
public class OrderService {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private OrderRepository orderRepository;

  private Optional<MeterRegistry> meterRegistry;

  private ConsumerServiceClient consumerServiceClient;
  private RestaurantServiceClient restaurantServiceClient;
  private CourierServiceClient courierServiceClient;

  public OrderService(OrderRepository orderRepository,
                      Optional<MeterRegistry> meterRegistry,
                      ConsumerServiceClient consumerServiceClient,
                      RestaurantServiceClient restaurantServiceClient,
                      CourierServiceClient courierServiceClient) {

    this.orderRepository = orderRepository;
    this.meterRegistry = meterRegistry;
    this.consumerServiceClient = consumerServiceClient;
    this.restaurantServiceClient = restaurantServiceClient;
    this.courierServiceClient = courierServiceClient;
  }

  @Transactional
  public Order createOrder(long consumerId, long restaurantId,
                           List<MenuItemIdAndQuantity> lineItems) {
    GetRestaurantResponse restaurant = restaurantServiceClient.findById(restaurantId);
    if (restaurant == null) {
      throw new RestaurantNotFoundException(restaurantId);
    }

    List<OrderLineItem> orderLineItems = makeOrderLineItems(lineItems, restaurant);

    Order order = new Order(consumerId, restaurantId, restaurant.getName(), orderLineItems);

    consumerServiceClient.validateOrderForConsumer(consumerId, order.getOrderTotal());

    orderRepository.save(order);

    meterRegistry.ifPresent(mr1 -> mr1.counter("approved_orders").increment());

    meterRegistry.ifPresent(mr -> mr.counter("placed_orders").increment());

    return order;
  }

  private List<OrderLineItem> makeOrderLineItems(List<MenuItemIdAndQuantity> lineItems, GetRestaurantResponse restaurant) {
    return lineItems.stream().map(li -> {
      MenuItemDTO om = restaurant.getMenuItems().stream()
              .filter(mi -> mi.getId().equals(li.getMenuItemId()))
              .findFirst()
              .orElseThrow(() -> new InvalidMenuItemIdException(li.getMenuItemId()));
      return new OrderLineItem(li.getMenuItemId(), om.getName(), om.getPrice(), li.getQuantity());
    }).collect(toList());
  }

  @Transactional
  public Order cancel(Long orderId) {
    Order order = tryToFindOrder(orderId);

    order.cancel();

    return order;
  }

  @Transactional
  public Order reviseOrder(long orderId, OrderRevision orderRevision) {
    Order order = tryToFindOrder(orderId);
    order.revise(orderRevision);
    return order;
  }

  public void accept(long orderId, LocalDateTime readyBy) {
    Order order = tryToFindOrder(orderId);
    order.acceptTicket(readyBy);

    long courierId = courierServiceClient.assignDelivery(orderId, readyBy);
    order.schedule(courierId);
  }

  private Order tryToFindOrder(Long orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
  }

  @Transactional
  public void notePreparing(long orderId) {
    Order order = tryToFindOrder(orderId);
    order.notePreparing();
  }

  @Transactional
  public void noteReadyForPickup(long orderId) {
    Order order = tryToFindOrder(orderId);
    order.noteReadyForPickup();
  }

  @Transactional
  public void notePickedUp(long orderId) {
    Order order = tryToFindOrder(orderId);
    order.notePickedUp();
  }

  @Transactional
  public void noteDelivered(long orderId) {
    Order order = tryToFindOrder(orderId);
    order.noteDelivered();
  }
}
