package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.courierservice.api.CourierNotFoundException;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.client.CourierServiceProxy;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

public class OrderService {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private OrderRepository orderRepository;

  private RestaurantRepository restaurantRepository;

  private Optional<MeterRegistry> meterRegistry;

  private ConsumerService consumerService;
  private CourierServiceProxy courierServiceProxy;
  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;
  private TransactionTemplate transactionTemplate;

  public OrderService(OrderRepository orderRepository,
                      RestaurantRepository restaurantRepository,
                      Optional<MeterRegistry> meterRegistry,
                      ConsumerService consumerService,
                      CourierServiceProxy courierServiceProxy,
                      CourierRepository courierRepository,
                      CourierAssignmentStrategy courierAssignmentStrategy,
                      PlatformTransactionManager transactionManager) {

    this.orderRepository = orderRepository;
    this.restaurantRepository = restaurantRepository;
    this.meterRegistry = meterRegistry;
    this.consumerService = consumerService;
    this.courierServiceProxy = courierServiceProxy;
    this.courierRepository = courierRepository;
    this.courierAssignmentStrategy = courierAssignmentStrategy;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
  }

  @Transactional
  public Order createOrder(long consumerId, long restaurantId,
                           List<MenuItemIdAndQuantity> lineItems) {
    Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));


    List<OrderLineItem> orderLineItems = makeOrderLineItems(lineItems, restaurant);

    Order order = new Order(consumerId, restaurant, orderLineItems);

    consumerService.validateOrderForConsumer(consumerId, order.getOrderTotal());

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
    // Fetch available couriers from the extracted courier service over HTTP BEFORE opening a
    // database transaction. Doing the remote call inside the transaction would hold a DB
    // connection open for the duration of the (up to 5s) HTTP request and could exhaust the
    // connection pool under load. The DB writes below run in their own short transaction.
    List<Courier> availableCouriers = courierServiceProxy.findAllAvailable();

    transactionTemplate.execute(status -> {
      Order order = tryToFindOrder(orderId);
      order.acceptTicket(readyBy);
      scheduleDelivery(order, readyBy, availableCouriers);
      return null;
    });
  }

  public void scheduleDelivery(Order order, LocalDateTime readyBy, List<Courier> couriers) {
    Courier assignedCourier = courierAssignmentStrategy.assignCourier(couriers, order);

    // The availability read is served by the extracted courier service over HTTP, but the
    // Order/Action coupling stays in-process: re-load the chosen courier as a JPA-managed entity
    // so the action additions and the Order->Courier association are persisted by dirty checking.
    Courier courier = courierRepository.findById(assignedCourier.getId())
            .orElseThrow(() -> new CourierNotFoundException(assignedCourier.getId()));

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
