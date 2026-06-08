package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.client.RestaurantServiceProxy;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantDTO;
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

  private RestaurantServiceProxy restaurantServiceProxy;

  private Optional<MeterRegistry> meterRegistry;

  private ConsumerService consumerService;
  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;

  public OrderService(OrderRepository orderRepository,
                      RestaurantServiceProxy restaurantServiceProxy,
                      Optional<MeterRegistry> meterRegistry,
                      ConsumerService consumerService,
                      CourierRepository courierRepository,
                      CourierAssignmentStrategy courierAssignmentStrategy) {

    this.orderRepository = orderRepository;
    this.restaurantServiceProxy = restaurantServiceProxy;
    this.meterRegistry = meterRegistry;
    this.consumerService = consumerService;
    this.courierRepository = courierRepository;
    this.courierAssignmentStrategy = courierAssignmentStrategy;
  }

  @Transactional
  public Order createOrder(long consumerId, long restaurantId,
                           List<MenuItemIdAndQuantity> lineItems) {
    RestaurantDTO restaurant = restaurantServiceProxy.findRestaurant(restaurantId);

    List<OrderLineItem> orderLineItems = makeOrderLineItems(lineItems, restaurant.getMenuItems());

    Order order = new Order(consumerId, restaurantId, restaurant.getName(), restaurant.getAddress(), orderLineItems);

    consumerService.validateOrderForConsumer(consumerId, order.getOrderTotal());

    orderRepository.save(order);

    meterRegistry.ifPresent(mr1 -> mr1.counter("approved_orders").increment());

    meterRegistry.ifPresent(mr -> mr.counter("placed_orders").increment());

    return order;
  }

  private List<OrderLineItem> makeOrderLineItems(List<MenuItemIdAndQuantity> lineItems, List<MenuItemDTO> menuItems) {
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
    scheduleDelivery(order, readyBy);
  }

  public void scheduleDelivery(Order order, LocalDateTime readyBy) {
    List<Courier> couriers = courierRepository.findAllAvailable();
    Courier courier = courierAssignmentStrategy.assignCourier(couriers, order);

    courier.addAction(Action.makePickup(order));

    LocalDateTime estimatedDeliveryTime = estimateDeliveryTime(courier, order, readyBy);
    courier.addAction(Action.makeDropoff(order, estimatedDeliveryTime));

    order.schedule(courier);

    logger.info("Order {} assigned to courier {} (active deliveries: {}, ETA: {})",
            order.getId(), courier.getId(), courier.getActiveDeliveryCount(), estimatedDeliveryTime);

    meterRegistry.ifPresent(mr -> mr.counter("courier_assignments").increment());
  }

  private LocalDateTime estimateDeliveryTime(Courier courier, Order order, LocalDateTime readyBy) {
    if (courier.hasLocation() && order.getRestaurantAddress() != null
            && order.getRestaurantAddress().getLatitude() != null) {

      double pickupDistance = DistanceOptimizedCourierAssignmentStrategy.haversineDistance(
              courier.getCurrentLatitude(), courier.getCurrentLongitude(),
              order.getRestaurantAddress().getLatitude(),
              order.getRestaurantAddress().getLongitude());

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
