package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.security.FtgoPrincipal;
import net.chrisrichardson.ftgo.domain.*;
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
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "/orders")
public class OrderController {

  private OrderService orderService;

  private OrderRepository orderRepository;

  private OrderAccessPolicy accessPolicy;

  public OrderController(OrderService orderService, OrderRepository orderRepository, OrderAccessPolicy accessPolicy) {
    this.orderService = orderService;
    this.orderRepository = orderRepository;
    this.accessPolicy = accessPolicy;
  }

  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<CreateOrderResponse> create(@RequestBody CreateOrderRequest request,
                                                    @AuthenticationPrincipal FtgoPrincipal principal) {
    if (!accessPolicy.canCreateOrderFor(principal, request.getConsumerId()))
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    Order order = orderService.createOrder(request.getConsumerId(),
            request.getRestaurantId(),
            request.getLineItems().stream().map(x -> new MenuItemIdAndQuantity(x.getMenuItemId(), x.getQuantity())).collect(toList())
    );
    return new ResponseEntity<>(new CreateOrderResponse(order.getId()), HttpStatus.OK);
  }


  @RequestMapping(path = "/{orderId}", method = RequestMethod.GET)
  public ResponseEntity<GetOrderResponse> getOrder(@PathVariable long orderId) {
    Optional<Order> order = orderRepository.findById(orderId);
    return order.map(o -> new ResponseEntity<>(makeGetOrderResponse(o), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<GetOrderResponse>> getOrders(@RequestParam long consumerId) {
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
  public ResponseEntity<GetOrderResponse> cancel(@PathVariable long orderId, @AuthenticationPrincipal FtgoPrincipal principal) {
    return withAuthorizedOrder(orderId, principal, accessPolicy::canActAsConsumer,
            order -> new ResponseEntity<>(makeGetOrderResponse(orderService.cancel(orderId)), HttpStatus.OK));
  }

  @RequestMapping(path = "/{orderId}/revise", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> revise(@PathVariable long orderId, @RequestBody ReviseOrderRequest request,
                                                 @AuthenticationPrincipal FtgoPrincipal principal) {
    return withAuthorizedOrder(orderId, principal, accessPolicy::canActAsConsumer, order -> {
      Order revised = orderService.reviseOrder(orderId, new OrderRevision(Optional.empty(), request.getRevisedLineItemQuantities()));
      return new ResponseEntity<>(makeGetOrderResponse(revised), HttpStatus.OK);
    });
  }

  @RequestMapping(path="/{orderId}/accept", method= RequestMethod.POST)
  public ResponseEntity<String> accept(@PathVariable long orderId, @RequestBody OrderAcceptance orderAcceptance,
                                       @AuthenticationPrincipal FtgoPrincipal principal) {
    return withAuthorizedOrder(orderId, principal, accessPolicy::canActAsRestaurant, order -> {
      orderService.accept(orderId, orderAcceptance.getReadyBy());
      return new ResponseEntity<>(HttpStatus.OK);
    });
  }

  @RequestMapping(path="/{orderId}/preparing", method= RequestMethod.POST)
  public ResponseEntity<String> preparing(@PathVariable long orderId, @AuthenticationPrincipal FtgoPrincipal principal) {
    return withAuthorizedOrder(orderId, principal, accessPolicy::canActAsRestaurant, order -> {
      orderService.notePreparing(orderId);
      return new ResponseEntity<>(HttpStatus.OK);
    });
  }

  @RequestMapping(path="/{orderId}/ready", method= RequestMethod.POST)
  public ResponseEntity<String> ready(@PathVariable long orderId, @AuthenticationPrincipal FtgoPrincipal principal) {
    return withAuthorizedOrder(orderId, principal, accessPolicy::canActAsRestaurant, order -> {
      orderService.noteReadyForPickup(orderId);
      return new ResponseEntity<>(HttpStatus.OK);
    });
  }

  @RequestMapping(path="/{orderId}/pickedup", method= RequestMethod.POST)
  public ResponseEntity<String> pickedup(@PathVariable long orderId, @AuthenticationPrincipal FtgoPrincipal principal) {
    return withAuthorizedOrder(orderId, principal, accessPolicy::canActAsCourier, order -> {
      orderService.notePickedUp(orderId);
      return new ResponseEntity<>(HttpStatus.OK);
    });
  }

  @RequestMapping(path="/{orderId}/delivered", method= RequestMethod.POST)
  public ResponseEntity<String> delivered(@PathVariable long orderId, @AuthenticationPrincipal FtgoPrincipal principal) {
    return withAuthorizedOrder(orderId, principal, accessPolicy::canActAsCourier, order -> {
      orderService.noteDelivered(orderId);
      return new ResponseEntity<>(HttpStatus.OK);
    });
  }

  private <T> ResponseEntity<T> withAuthorizedOrder(long orderId, FtgoPrincipal principal,
                                                    BiPredicate<FtgoPrincipal, Order> permitted,
                                                    Function<Order, ResponseEntity<T>> action) {
    Optional<Order> order = orderRepository.findById(orderId);
    if (!order.isPresent())
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    if (!permitted.test(principal, order.get()))
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    try {
      return action.apply(order.get());
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

}
