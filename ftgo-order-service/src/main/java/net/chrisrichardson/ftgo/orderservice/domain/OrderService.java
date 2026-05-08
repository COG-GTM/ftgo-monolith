package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Transactional
public class OrderService {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private OrderRepository orderRepository;

  private RestaurantServiceClient restaurantServiceClient;

  private Optional<MeterRegistry> meterRegistry;

  private ConsumerService consumerService;
  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;

  public OrderService(OrderRepository orderRepository,
                      RestaurantServiceClient restaurantServiceClient,
                      Optional<MeterRegistry> meterRegistry,
                      ConsumerService consumerService,
                      CourierRepository courierRepository,
                      CourierAssignmentStrategy courierAssignmentStrategy) {

    this.orderRepository = orderRepository;
    this.restaurantServiceClient = restaurantServiceClient;
    this.meterRegistry = meterRegistry;
    this.consumerService = consumerService;
    this.courierRepository = courierRepository;
    this.courierAssignmentStrategy = courierAssignmentStrategy;
  }

  @Transactional
  public Order createOrder(long consumerId, long restaurantId,
                           List<MenuItemIdAndQuantity> lineItems) {
    RestaurantDTO restaurant = restaurantServiceClient.findById(restaurantId);
    if (restaurant == null) {
      throw new RestaurantNotFoundException(restaurantId);
    }

    List<OrderLineItem> orderLineItems = makeOrderLineItems(lineItems, restaurant);

    Order order = new Order(consumerId, restaurant.getId(), restaurant.getName(), orderLineItems);

    consumerService.validateOrderForConsumer(consumerId, order.getOrderTotal());

    // TODO - charge a credit card too

    orderRepository.save(order);

    meterRegistry.ifPresent(mr1 -> mr1.counter("approved_orders").increment());

    meterRegistry.ifPresent(mr -> mr.counter("placed_orders").increment());

    return order;
  }

  private List<OrderLineItem> makeOrderLineItems(List<MenuItemIdAndQuantity> lineItems, RestaurantDTO restaurant) {
    List<MenuItemDTO> menuItems = restaurant.getMenuItems() == null
            ? Collections.emptyList() : restaurant.getMenuItems();
    return lineItems.stream().map(li -> {
      MenuItemDTO om = menuItems.stream()
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
    Address restaurantAddress = lookupRestaurantAddress(order.getRestaurantId());
    scheduleDelivery(order, readyBy, restaurantAddress);
  }

  public void scheduleDelivery(Order order, LocalDateTime readyBy, Address restaurantAddress) {
    List<Courier> couriers = courierRepository.findAllAvailable();
    Courier courier = courierAssignmentStrategy.assignCourier(couriers, order, restaurantAddress);

    courier.addAction(Action.makePickup(order));

    LocalDateTime estimatedDeliveryTime = estimateDeliveryTime(courier, restaurantAddress, readyBy);
    courier.addAction(Action.makeDropoff(order, estimatedDeliveryTime));

    order.schedule(courier);

    logger.info("Order {} assigned to courier {} (active deliveries: {}, ETA: {})",
            order.getId(), courier.getId(), courier.getActiveDeliveryCount(), estimatedDeliveryTime);

    meterRegistry.ifPresent(mr -> mr.counter("courier_assignments").increment());
  }

  private Address lookupRestaurantAddress(Long restaurantId) {
    if (restaurantId == null) {
      return null;
    }
    try {
      RestaurantDTO restaurant = restaurantServiceClient.findById(restaurantId);
      return restaurant == null ? null : restaurant.getAddress();
    } catch (RuntimeException e) {
      logger.warn("Failed to lookup restaurant {} for delivery scheduling: {}", restaurantId, e.getMessage());
      return null;
    }
  }

  private LocalDateTime estimateDeliveryTime(Courier courier, Address restaurantAddress, LocalDateTime readyBy) {
    if (courier.hasLocation() && restaurantAddress != null
            && restaurantAddress.getLatitude() != null) {

      double pickupDistance = DistanceOptimizedCourierAssignmentStrategy.haversineDistance(
              courier.getCurrentLatitude(), courier.getCurrentLongitude(),
              restaurantAddress.getLatitude(),
              restaurantAddress.getLongitude());

      long pickupMinutes = (long) DistanceOptimizedCourierAssignmentStrategy.estimateDeliveryMinutes(pickupDistance);
      LocalDateTime pickupArrival = LocalDateTime.now().plusMinutes(pickupMinutes);
      LocalDateTime effectiveReadyTime = pickupArrival.isAfter(readyBy) ? pickupArrival : readyBy;

      return effectiveReadyTime.plusMinutes(15);
    }

    return readyBy.plusMinutes(30);
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
