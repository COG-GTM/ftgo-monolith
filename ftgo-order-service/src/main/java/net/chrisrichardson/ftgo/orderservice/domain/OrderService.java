package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.consumerservice.api.ConsumerValidationApi;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.courierservice.api.CourierAssignment;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailabilityApi;
import net.chrisrichardson.ftgo.restaurantservice.api.RestaurantDetails;
import net.chrisrichardson.ftgo.restaurantservice.api.RestaurantLookupApi;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

@Transactional(OrderPersistenceConfiguration.TRANSACTION_MANAGER)
public class OrderService {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private OrderRepository orderRepository;

  private RestaurantLookupApi restaurantLookupApi;

  private Optional<MeterRegistry> meterRegistry;

  private ConsumerValidationApi consumerValidationApi;
  private CourierAvailabilityApi courierAvailabilityApi;

  public OrderService(OrderRepository orderRepository,
                      RestaurantLookupApi restaurantLookupApi,
                      Optional<MeterRegistry> meterRegistry,
                      ConsumerValidationApi consumerValidationApi,
                      CourierAvailabilityApi courierAvailabilityApi) {

    this.orderRepository = orderRepository;
    this.restaurantLookupApi = restaurantLookupApi;
    this.meterRegistry = meterRegistry;
    this.consumerValidationApi = consumerValidationApi;
    this.courierAvailabilityApi = courierAvailabilityApi;
  }

  @Transactional(OrderPersistenceConfiguration.TRANSACTION_MANAGER)
  public Order createOrder(long consumerId, long restaurantId,
                           List<MenuItemIdAndQuantity> lineItems) {
    RestaurantDetails restaurant = restaurantLookupApi.findRestaurantById(restaurantId)
            .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));


    List<OrderLineItem> orderLineItems = makeOrderLineItems(lineItems, restaurant);

    Order order = new Order(consumerId, restaurantId, orderLineItems);

    consumerValidationApi.validateOrderForConsumer(consumerId, order.getOrderTotal());

    // TODO - charge a credit card too

    orderRepository.save(order);

    meterRegistry.ifPresent(mr1 -> mr1.counter("approved_orders").increment());

    meterRegistry.ifPresent(mr -> mr.counter("placed_orders").increment());

    return order;
  }

  private List<OrderLineItem> makeOrderLineItems(List<MenuItemIdAndQuantity> lineItems, RestaurantDetails restaurant) {
    return lineItems.stream().map(li -> {
      MenuItemDTO om = restaurant.findMenuItem(li.getMenuItemId()).orElseThrow(() -> new InvalidMenuItemIdException(li.getMenuItemId()));
      return new OrderLineItem(li.getMenuItemId(), om.getName(), om.getPrice(), li.getQuantity());
    }).collect(toList());
  }

  @Transactional(OrderPersistenceConfiguration.TRANSACTION_MANAGER)
  public Order cancel(Long orderId) {
    Order order = tryToFindOrder(orderId);

    order.cancel();

    return order;
  }

  @Transactional(OrderPersistenceConfiguration.TRANSACTION_MANAGER)
  public Order reviseOrder(long orderId, OrderRevision orderRevision) {
    Order order = tryToFindOrder(orderId);
    order.revise(orderRevision);
    return order;
  }

  public void accept(long orderId, LocalDateTime readyBy) {
    Order order = tryToFindOrder(orderId);
    order.acceptTicket(readyBy);
    scheduleDelivery(order, readyBy);
  }

  public void scheduleDelivery(Order order, LocalDateTime readyBy) {
    RestaurantDetails restaurant = restaurantLookupApi.findRestaurantById(order.getRestaurantId())
            .orElseThrow(() -> new RestaurantNotFoundException(order.getRestaurantId()));
    Address address = restaurant.getAddress();
    CourierAssignment assignment = courierAvailabilityApi.assignCourierForDelivery(order.getId(),
            address == null ? null : address.getLatitude(),
            address == null ? null : address.getLongitude(),
            readyBy);
    order.schedule(assignment.getCourierId());
    meterRegistry.ifPresent(mr -> mr.counter("courier_assignments").increment());
  }

  private Order tryToFindOrder(Long orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
  }

  @Transactional(OrderPersistenceConfiguration.TRANSACTION_MANAGER)
  public void notePreparing(long orderId) {
    Order order = tryToFindOrder(orderId);
    order.notePreparing();
  }

  @Transactional(OrderPersistenceConfiguration.TRANSACTION_MANAGER)
  public void noteReadyForPickup(long orderId) {
    Order order = tryToFindOrder(orderId);
    order.noteReadyForPickup();
  }

  @Transactional(OrderPersistenceConfiguration.TRANSACTION_MANAGER)
  public void notePickedUp(long orderId) {
    Order order = tryToFindOrder(orderId);
    order.notePickedUp();
  }

  @Transactional(OrderPersistenceConfiguration.TRANSACTION_MANAGER)
  public void noteDelivered(long orderId) {
    Order order = tryToFindOrder(orderId);
    order.noteDelivered();
  }
}
