package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.common.security.FtgoRole;
import net.chrisrichardson.ftgo.common.security.FtgoUser;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
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
  public ResponseEntity<CreateOrderResponse> create(@AuthenticationPrincipal FtgoUser caller,
                                                     @RequestBody CreateOrderRequest request) {
    if (caller == null) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    long consumerId;
    if (caller.getRole() == FtgoRole.CONSUMER) {
      if (request.getConsumerId() != 0 && request.getConsumerId() != caller.getSubjectId()) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
      consumerId = caller.getSubjectId();
    } else if (caller.isAdmin()) {
      consumerId = request.getConsumerId();
    } else {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    Order order = orderService.createOrder(consumerId,
            request.getRestaurantId(),
            request.getLineItems().stream().map(x -> new MenuItemIdAndQuantity(x.getMenuItemId(), x.getQuantity())).collect(toList())
    );
    return new ResponseEntity<>(new CreateOrderResponse(order.getId()), HttpStatus.OK);
  }


  @RequestMapping(path = "/{orderId}", method = RequestMethod.GET)
  public ResponseEntity<GetOrderResponse> getOrder(@PathVariable long orderId, @AuthenticationPrincipal FtgoUser caller) {
    OrderAuthorizationResult authorization = authorizeOrder(orderId, caller, order -> true);
    if (authorization.status != null) {
      return new ResponseEntity<>(authorization.status);
    }
    return new ResponseEntity<>(makeGetOrderResponse(authorization.order), HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<GetOrderResponse>> getOrders(@RequestParam(required = false) Long consumerId,
                                                          @AuthenticationPrincipal FtgoUser caller) {
    if (caller == null) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    if (caller.getRole() == FtgoRole.CONSUMER) {
      if (consumerId != null && consumerId.longValue() != caller.getSubjectId()) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
      consumerId = caller.getSubjectId();
    } else if (caller.isAdmin()) {
      if (consumerId == null) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } else {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    List<GetOrderResponse> orders = orderRepository.findAllByConsumerId(consumerId)
            .stream()
            .map(this::makeGetOrderResponse)
            .collect(Collectors.toList());

    return new ResponseEntity<>(orders, HttpStatus.OK);
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
  public ResponseEntity<GetOrderResponse> cancel(@PathVariable long orderId, @AuthenticationPrincipal FtgoUser caller) {
    OrderAuthorizationResult authorization = authorizeOrder(orderId, caller, order -> OrderAuthorization.canActAsConsumer(caller, order));
    if (authorization.status != null) {
      return new ResponseEntity<>(authorization.status);
    }
    try {
      Order order = orderService.cancel(orderId);
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(path = "/{orderId}/revise", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> revise(@PathVariable long orderId, @RequestBody ReviseOrderRequest request,
                                                  @AuthenticationPrincipal FtgoUser caller) {
    OrderAuthorizationResult authorization = authorizeOrder(orderId, caller, order -> OrderAuthorization.canActAsConsumer(caller, order));
    if (authorization.status != null) {
      return new ResponseEntity<>(authorization.status);
    }
    try {
      Order order = orderService.reviseOrder(orderId, new OrderRevision(Optional.empty(), request.getRevisedLineItemQuantities()));
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(path="/{orderId}/accept", method= RequestMethod.POST)
  public ResponseEntity<String> accept(@PathVariable long orderId, @RequestBody OrderAcceptance orderAcceptance,
                                       @AuthenticationPrincipal FtgoUser caller) {
    OrderAuthorizationResult authorization = authorizeOrder(orderId, caller, order -> OrderAuthorization.canActAsRestaurant(caller, order));
    if (authorization.status != null) {
      return new ResponseEntity<>(authorization.status);
    }
    try {
      orderService.accept(orderId, orderAcceptance.getReadyBy());
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(path="/{orderId}/preparing", method= RequestMethod.POST)
  public ResponseEntity<String> preparing(@PathVariable long orderId, @AuthenticationPrincipal FtgoUser caller) {
    OrderAuthorizationResult authorization = authorizeOrder(orderId, caller, order -> OrderAuthorization.canActAsRestaurant(caller, order));
    if (authorization.status != null) {
      return new ResponseEntity<>(authorization.status);
    }
    try {
      orderService.notePreparing(orderId);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(path="/{orderId}/ready", method= RequestMethod.POST)
  public ResponseEntity<String> ready(@PathVariable long orderId, @AuthenticationPrincipal FtgoUser caller) {
    OrderAuthorizationResult authorization = authorizeOrder(orderId, caller, order -> OrderAuthorization.canActAsRestaurant(caller, order));
    if (authorization.status != null) {
      return new ResponseEntity<>(authorization.status);
    }
    try {
      orderService.noteReadyForPickup(orderId);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(path="/{orderId}/pickedup", method= RequestMethod.POST)
  public ResponseEntity<String> pickedup(@PathVariable long orderId, @AuthenticationPrincipal FtgoUser caller) {
    OrderAuthorizationResult authorization = authorizeOrder(orderId, caller, order -> OrderAuthorization.canActAsCourier(caller, order));
    if (authorization.status != null) {
      return new ResponseEntity<>(authorization.status);
    }
    try {
      orderService.notePickedUp(orderId);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(path="/{orderId}/delivered", method= RequestMethod.POST)
  public ResponseEntity<String> delivered(@PathVariable long orderId, @AuthenticationPrincipal FtgoUser caller) {
    OrderAuthorizationResult authorization = authorizeOrder(orderId, caller, order -> OrderAuthorization.canActAsCourier(caller, order));
    if (authorization.status != null) {
      return new ResponseEntity<>(authorization.status);
    }
    try {
      orderService.noteDelivered(orderId);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  private OrderAuthorizationResult authorizeOrder(long orderId, FtgoUser caller, java.util.function.Predicate<Order> actionAuthorization) {
    if (caller == null) {
      return new OrderAuthorizationResult(null, HttpStatus.UNAUTHORIZED);
    }
    Optional<Order> order = orderRepository.findById(orderId);
    if (!order.isPresent() || !OrderAuthorization.canView(caller, order.get())) {
      return new OrderAuthorizationResult(null, HttpStatus.NOT_FOUND);
    }
    if (!actionAuthorization.test(order.get())) {
      return new OrderAuthorizationResult(null, HttpStatus.FORBIDDEN);
    }
    return new OrderAuthorizationResult(order.get(), null);
  }

  private static class OrderAuthorizationResult {
    private final Order order;
    private final HttpStatus status;

    private OrderAuthorizationResult(Order order, HttpStatus status) {
      this.order = order;
      this.status = status;
    }
  }

}
