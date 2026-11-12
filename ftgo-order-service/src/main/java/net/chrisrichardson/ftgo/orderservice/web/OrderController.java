package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.security.FtgoUser;
import net.chrisrichardson.ftgo.security.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

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
  private OrderAccessPolicy orderAccessPolicy;


  public OrderController(OrderService orderService, OrderRepository orderRepository, OrderAccessPolicy orderAccessPolicy) {
    this.orderService = orderService;
    this.orderRepository = orderRepository;
    this.orderAccessPolicy = orderAccessPolicy;
  }

  @RequestMapping(method = RequestMethod.POST)
  public CreateOrderResponse create(@ApiIgnore @AuthenticationPrincipal FtgoUser caller,
                                    @RequestBody CreateOrderRequest request) {
    long consumerId;
    if (caller.getRole() == Role.CONSUMER) {
      consumerId = caller.getEntityId();
      if (request.getConsumerId() != 0 && request.getConsumerId() != consumerId) {
        throw new AccessDeniedException("Consumers may only create orders for themselves");
      }
    } else if (caller.isOperator()) {
      if (request.getConsumerId() == 0) {
        throw new IllegalArgumentException("Consumer id is required for operators");
      }
      consumerId = request.getConsumerId();
    } else {
      throw new AccessDeniedException("Only consumers and operators can create orders");
    }
    Order order = orderService.createOrder(consumerId,
            request.getRestaurantId(),
            request.getLineItems().stream().map(x -> new MenuItemIdAndQuantity(x.getMenuItemId(), x.getQuantity())).collect(toList())
    );
    return new CreateOrderResponse(order.getId());
  }


  @RequestMapping(path = "/{orderId}", method = RequestMethod.GET)
  public ResponseEntity<GetOrderResponse> getOrder(@ApiIgnore @AuthenticationPrincipal FtgoUser caller,
                                                   @PathVariable long orderId) {
    Order order = findOrder(orderId);
    orderAccessPolicy.checkCanRead(caller, order);
    return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<GetOrderResponse>> getOrders(@ApiIgnore @AuthenticationPrincipal FtgoUser caller,
                                                          @RequestParam(required = false) Long consumerId) {
    if (caller.getRole() == Role.CONSUMER) {
      if (consumerId != null && !caller.isConsumer(consumerId)) {
        throw new AccessDeniedException("Consumers may only list their own orders");
      }
      consumerId = caller.getEntityId();
    } else if (caller.isOperator()) {
      if (consumerId == null) {
        throw new IllegalArgumentException("Consumer id is required for operators");
      }
    } else {
      throw new AccessDeniedException("Only consumers and operators can list orders");
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
  public ResponseEntity<GetOrderResponse> cancel(@ApiIgnore @AuthenticationPrincipal FtgoUser caller,
                                                 @PathVariable long orderId) {
    Order order = findOrder(orderId);
    orderAccessPolicy.checkIsConsumer(caller, order);
    order = orderService.cancel(orderId);
    return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
  }

  @RequestMapping(path = "/{orderId}/revise", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> revise(@ApiIgnore @AuthenticationPrincipal FtgoUser caller,
                                                 @PathVariable long orderId, @RequestBody ReviseOrderRequest request) {
    Order order = findOrder(orderId);
    orderAccessPolicy.checkIsConsumer(caller, order);
    order = orderService.reviseOrder(orderId, new OrderRevision(Optional.empty(), request.getRevisedLineItemQuantities()));
    return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
  }

  @RequestMapping(path="/{orderId}/accept", method= RequestMethod.POST)
  public ResponseEntity<String> accept(@ApiIgnore @AuthenticationPrincipal FtgoUser caller,
                                       @PathVariable long orderId, @RequestBody OrderAcceptance orderAcceptance) {
    Order order = findOrder(orderId);
    orderAccessPolicy.checkIsRestaurant(caller, order);
    orderService.accept(orderId, orderAcceptance.getReadyBy());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/{orderId}/preparing", method= RequestMethod.POST)
  public ResponseEntity<String> preparing(@ApiIgnore @AuthenticationPrincipal FtgoUser caller, @PathVariable long orderId) {
    Order order = findOrder(orderId);
    orderAccessPolicy.checkIsRestaurant(caller, order);
    orderService.notePreparing(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/{orderId}/ready", method= RequestMethod.POST)
  public ResponseEntity<String> ready(@ApiIgnore @AuthenticationPrincipal FtgoUser caller, @PathVariable long orderId) {
    Order order = findOrder(orderId);
    orderAccessPolicy.checkIsRestaurant(caller, order);
    orderService.noteReadyForPickup(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/{orderId}/pickedup", method= RequestMethod.POST)
  public ResponseEntity<String> pickedup(@ApiIgnore @AuthenticationPrincipal FtgoUser caller, @PathVariable long orderId) {
    Order order = findOrder(orderId);
    orderAccessPolicy.checkIsAssignedCourier(caller, order);
    orderService.notePickedUp(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/{orderId}/delivered", method= RequestMethod.POST)
  public ResponseEntity<String> delivered(@ApiIgnore @AuthenticationPrincipal FtgoUser caller, @PathVariable long orderId) {
    Order order = findOrder(orderId);
    orderAccessPolicy.checkIsAssignedCourier(caller, order);
    orderService.noteDelivered(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  private Order findOrder(long orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
  }
}
