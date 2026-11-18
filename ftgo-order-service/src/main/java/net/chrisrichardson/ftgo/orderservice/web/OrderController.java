package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "/orders")
public class OrderController {

  private OrderService orderService;

  private OrderRepository orderRepository;


  public OrderController(OrderService orderService, OrderRepository orderRepository) {
    this.orderService = orderService;
    this.orderRepository = orderRepository;
  }

  @RequestMapping(method = RequestMethod.POST)
  public CreateOrderResponse create(@RequestBody CreateOrderRequest request) {
    Order order = orderService.createOrder(request.getConsumerId(),
            request.getRestaurantId(),
            request.getLineItems().stream().map(x -> new MenuItemIdAndQuantity(x.getMenuItemId(), x.getQuantity())).collect(toList())
    );
    return new CreateOrderResponse(order.getId());
  }


  @RequestMapping(path = "/{orderId}", method = RequestMethod.GET)
  public ResponseEntity<GetOrderResponse> getOrder(@PathVariable long orderId) {
    Optional<Order> order = orderRepository.findById(orderId);
    return order.map(o -> new ResponseEntity<>(makeGetOrderResponse(o), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  static final int DEFAULT_PAGE_SIZE = 20;
  static final int MAX_PAGE_SIZE = 100;

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<?> getOrders(@RequestParam(required = false) String consumerId,
                                     @RequestParam(required = false) String state,
                                     @RequestParam(required = false) String page,
                                     @RequestParam(required = false) String size) {
    long consumer = parseConsumerId(consumerId);
    OrderState orderState = parseState(state);
    int pageSize = parseSize(size);

    if (page == null) {
      List<Order> orders = orderState == null
              ? orderRepository.findAllByConsumerId(consumer)
              : orderRepository.findAllByConsumerIdAndOrderState(consumer, orderState);
      return new ResponseEntity<>(orders.stream().map(this::makeGetOrderResponse).collect(Collectors.toList()), HttpStatus.OK);
    }

    int pageNumber = parsePage(page);
    Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id"));
    Page<Order> orders = orderState == null
            ? orderRepository.findAllByConsumerId(consumer, pageable)
            : orderRepository.findAllByConsumerIdAndOrderState(consumer, orderState, pageable);
    List<GetOrderResponse> content = orders.getContent().stream().map(this::makeGetOrderResponse).collect(Collectors.toList());
    return new ResponseEntity<>(new GetOrdersPageResponse(content, pageNumber, pageSize, orders.getTotalElements()), HttpStatus.OK);
  }

  private long parseConsumerId(String consumerId) {
    if (consumerId == null || consumerId.trim().isEmpty())
      throw new IllegalArgumentException("consumerId is required");
    try {
      return Long.parseLong(consumerId.trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("consumerId must be a number: " + consumerId);
    }
  }

  private OrderState parseState(String state) {
    if (state == null)
      return null;
    try {
      return OrderState.valueOf(state.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("state must be one of " + Arrays.toString(OrderState.values()) + ": " + state);
    }
  }

  private int parsePage(String page) {
    int value = parseInt("page", page);
    if (value < 0)
      throw new IllegalArgumentException("page must be >= 0: " + page);
    return value;
  }

  private int parseSize(String size) {
    if (size == null)
      return DEFAULT_PAGE_SIZE;
    int value = parseInt("size", size);
    if (value < 1 || value > MAX_PAGE_SIZE)
      throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE + ": " + size);
    return value;
  }

  private int parseInt(String name, String value) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(name + " must be an integer: " + value);
    }
  }

  private GetOrderResponse makeGetOrderResponse(Order order) {
    List<Action> courierActions = order.getAssignedCourier() == null
            ? null
            : order.getAssignedCourier().actionsForDelivery(order);

    LocalDateTime estimatedDelivery = null;
    if (courierActions != null) {
      estimatedDelivery = courierActions.stream()
              .filter(a -> a.getType() == ActionType.DROPOFF)
              .map(Action::getTime)
              .findFirst()
              .orElse(null);
    }

    return new GetOrderResponse(order.getId(),
            order.getOrderState().name(),
            order.getOrderTotal(),
            order.getRestaurant().getName(),
            order.getAssignedCourier() == null ? null : order.getAssignedCourier().getId(),
            courierActions,
            estimatedDelivery
    );
  }

  @RequestMapping(path = "/{orderId}/cancel", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> cancel(@PathVariable long orderId) {
    try {
      Order order = orderService.cancel(orderId);
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(path = "/{orderId}/revise", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> revise(@PathVariable long orderId, @RequestBody ReviseOrderRequest request) {
    try {
      Order order = orderService.reviseOrder(orderId, new OrderRevision(Optional.empty(), request.getRevisedLineItemQuantities()));
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(path="/{orderId}/accept", method= RequestMethod.POST)
  public ResponseEntity<String> accept(@PathVariable long orderId, @RequestBody OrderAcceptance orderAcceptance) {
    orderService.accept(orderId, orderAcceptance.getReadyBy());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/{orderId}/preparing", method= RequestMethod.POST)
  public ResponseEntity<String> preparing(@PathVariable long orderId) {
    orderService.notePreparing(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/{orderId}/ready", method= RequestMethod.POST)
  public ResponseEntity<String> ready(@PathVariable long orderId) {
    orderService.noteReadyForPickup(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/{orderId}/pickedup", method= RequestMethod.POST)
  public ResponseEntity<String> pickedup(@PathVariable long orderId) {
    orderService.notePickedUp(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/{orderId}/delivered", method= RequestMethod.POST)
  public ResponseEntity<String> delivered(@PathVariable long orderId) {
    orderService.noteDelivered(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
