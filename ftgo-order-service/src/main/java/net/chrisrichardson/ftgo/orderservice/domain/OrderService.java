package net.chrisrichardson.ftgo.orderservice.domain;

import io.micrometer.core.instrument.MeterRegistry;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest.MAX_LINE_ITEMS;
import static net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest.MAX_QUANTITY_PER_LINE_ITEM;

@Transactional
public class OrderService {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private OrderRepository orderRepository;

  private RestaurantRepository restaurantRepository;

  private Optional<MeterRegistry> meterRegistry;

  private ConsumerService consumerService;
  private CourierRepository courierRepository;
  private CourierAssignmentStrategy courierAssignmentStrategy;

  public OrderService(OrderRepository orderRepository,
                      RestaurantRepository restaurantRepository,
                      Optional<MeterRegistry> meterRegistry,
                      ConsumerService consumerService,
                      CourierRepository courierRepository,
                      CourierAssignmentStrategy courierAssignmentStrategy) {

    this.orderRepository = orderRepository;
    this.restaurantRepository = restaurantRepository;
    this.meterRegistry = meterRegistry;
    this.consumerService = consumerService;
    this.courierRepository = courierRepository;
    this.courierAssignmentStrategy = courierAssignmentStrategy;
  }

  @Transactional
  public Order createOrder(long consumerId, long restaurantId,
                           List<MenuItemIdAndQuantity> lineItems) {
    validateLineItems(lineItems);

    Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));


    List<OrderLineItem> orderLineItems = makeOrderLineItems(lineItems, restaurant);

    Order order = new Order(consumerId, restaurant, orderLineItems);

    if (Money.ZERO.isGreaterThanOrEqual(order.getOrderTotal())) {
      throw new InvalidOrderException("Order total must be greater than zero");
    }

    consumerService.validateOrderForConsumer(consumerId, order.getOrderTotal());

    // TODO - charge a credit card too

    orderRepository.save(order);

    meterRegistry.ifPresent(mr1 -> mr1.counter("approved_orders").increment());

    meterRegistry.ifPresent(mr -> mr.counter("placed_orders").increment());

    return order;
  }

  private void validateLineItems(List<MenuItemIdAndQuantity> lineItems) {
    if (lineItems == null || lineItems.isEmpty()) {
      throw new InvalidOrderException("Order must contain at least one line item");
    }
    if (lineItems.size() > MAX_LINE_ITEMS) {
      throw new InvalidOrderException("Order may contain at most " + MAX_LINE_ITEMS + " line items");
    }

    Set<String> menuItemIds = new HashSet<>();
    for (MenuItemIdAndQuantity lineItem : lineItems) {
      if (lineItem == null) {
        throw new InvalidOrderException("Line item must not be null");
      }

      String menuItemId = lineItem.getMenuItemId();
      if (menuItemId == null || menuItemId.trim().isEmpty()) {
        throw new InvalidOrderException("Menu item id must not be blank");
      }
      if (lineItem.getQuantity() < 1 || lineItem.getQuantity() > MAX_QUANTITY_PER_LINE_ITEM) {
        throw new InvalidOrderException("Quantity for menu item " + menuItemId
                + " must be between 1 and " + MAX_QUANTITY_PER_LINE_ITEM);
      }
      if (!menuItemIds.add(menuItemId)) {
        throw new InvalidOrderException("Duplicate menu item id " + menuItemId);
      }
    }
  }

  private List<OrderLineItem> makeOrderLineItems(List<MenuItemIdAndQuantity> lineItems, Restaurant restaurant) {
    Map<String, MenuItem> menuItemsById = restaurant.menuItemsById();
    return lineItems.stream().map(li -> {
      MenuItem om = menuItemsById.get(li.getMenuItemId());
      if (om == null) {
        throw new InvalidMenuItemIdException(li.getMenuItemId());
      }
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
