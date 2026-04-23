package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.domain.proxy.ConsumerValidationService;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
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

  private RestaurantRepository restaurantRepository;

  private Optional<MeterRegistry> meterRegistry;

  private ConsumerValidationService consumerValidationService;
  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;

  public OrderService(OrderRepository orderRepository,
                      RestaurantRepository restaurantRepository,
                      Optional<MeterRegistry> meterRegistry,
                      ConsumerValidationService consumerValidationService,
                      CourierRepository courierRepository,
                      CourierAssignmentStrategy courierAssignmentStrategy) {

    this.orderRepository = orderRepository;
    this.restaurantRepository = restaurantRepository;
    this.meterRegistry = meterRegistry;
    this.consumerValidationService = consumerValidationService;
    this.courierRepository = courierRepository;
    this.courierAssignmentStrategy = courierAssignmentStrategy;
  }

  @Transactional
  public Order createOrder(long consumerId, long restaurantId,
                           List<MenuItemIdAndQuantity> lineItems) {
    Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));


    List<OrderLineItem> orderLineItems = makeOrderLineItems(lineItems, restaurant);

    Order order = new Order(consumerId, restaurant, orderLineItems);

    consumerValidationService.validateOrderForConsumer(consumerId, order.getOrderTotal());

    // TODO - charge a credit card too

    orderRepository.save(order);

    meterRegistry.ifPresent(mr1 -> mr1.counter("approved_orders").increment());

    meterRegistry.ifPresent(mr -> mr.counter("placed_orders").increment());

    return order;
  }

  private List<OrderLineItem> makeOrderLineItems(List<MenuItemIdAndQuantity> lineItems, Restaurant restaurant) {
    return lineItems.stream().map(li -> {
      MenuItem om = restaurant.findMenuItem(li.getMenuItemId()).orElseThrow(() -> new InvalidMenuItemIdException(li.getMenuItemId()));
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
    if (courier.hasLocation() && order.getRestaurant() != null
            && order.getRestaurant().getAddress() != null
            && order.getRestaurant().getAddress().getLatitude() != null) {

      double pickupDistance = DistanceOptimizedCourierAssignmentStrategy.haversineDistance(
              courier.getCurrentLatitude(), courier.getCurrentLongitude(),
              order.getRestaurant().getAddress().getLatitude(),
              order.getRestaurant().getAddress().getLongitude());

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
