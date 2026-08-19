package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.security.AccessDeniedException;
import net.chrisrichardson.ftgo.common.security.AuthenticatedConsumer;
import net.chrisrichardson.ftgo.common.security.AuthenticatedStaff;
import net.chrisrichardson.ftgo.common.security.StaffAuthenticator;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerAuthenticator;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "/orders")
public class OrderController {

  private static final String AUTHORIZATION_HEADER = "Authorization";

  private OrderService orderService;

  private OrderRepository orderRepository;

  private ConsumerAuthenticator consumerAuthenticator;

  private StaffAuthenticator staffAuthenticator;


  public OrderController(OrderService orderService, OrderRepository orderRepository,
                         ConsumerAuthenticator consumerAuthenticator, StaffAuthenticator staffAuthenticator) {
    this.orderService = orderService;
    this.orderRepository = orderRepository;
    this.consumerAuthenticator = consumerAuthenticator;
    this.staffAuthenticator = staffAuthenticator;
  }

  @RequestMapping(method = RequestMethod.POST)
  public CreateOrderResponse create(@RequestHeader(name = AUTHORIZATION_HEADER, required = false) String authorization,
                                    @RequestBody CreateOrderRequest request) {
    AuthenticatedConsumer consumer = consumerAuthenticator.authenticate(authorization);

    if (request.getConsumerId() != 0 && request.getConsumerId() != consumer.getConsumerId())
      throw new AccessDeniedException("Cannot create an order on behalf of another consumer");

    Order order = orderService.createOrder(consumer,
            request.getRestaurantId(),
            request.getLineItems().stream().map(x -> new MenuItemIdAndQuantity(x.getMenuItemId(), x.getQuantity())).collect(toList())
    );
    return new CreateOrderResponse(order.getId());
  }


  @RequestMapping(path = "/{orderId}", method = RequestMethod.GET)
  public ResponseEntity<GetOrderResponse> getOrder(@RequestHeader(name = AUTHORIZATION_HEADER, required = false) String authorization,
                                                   @PathVariable long orderId) {
    AuthenticatedConsumer consumer = consumerAuthenticator.authenticate(authorization);
    Optional<Order> order = orderRepository.findByIdAndConsumerId(orderId, consumer.getConsumerId());
    return order.map(o -> new ResponseEntity<>(makeGetOrderResponse(o), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<GetOrderResponse>> getOrders(@RequestHeader(name = AUTHORIZATION_HEADER, required = false) String authorization,
                                                          @RequestParam(required = false) Long consumerId) {
    AuthenticatedConsumer consumer = consumerAuthenticator.authenticate(authorization);

    if (consumerId != null && consumerId != consumer.getConsumerId())
      throw new AccessDeniedException("Cannot read the orders of another consumer");

    List<GetOrderResponse> orders = orderService.findOrdersOfConsumer(consumer)
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
  public ResponseEntity<GetOrderResponse> cancel(@RequestHeader(name = AUTHORIZATION_HEADER, required = false) String authorization,
                                                 @PathVariable long orderId) {
    try {
      Order order = orderService.cancel(orderId, consumerAuthenticator.authenticate(authorization));
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(path = "/{orderId}/revise", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> revise(@RequestHeader(name = AUTHORIZATION_HEADER, required = false) String authorization,
                                                 @PathVariable long orderId, @RequestBody ReviseOrderRequest request) {
    try {
      Order order = orderService.reviseOrder(orderId, new OrderRevision(Optional.empty(), request.getRevisedLineItemQuantities()),
              consumerAuthenticator.authenticate(authorization));
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(path="/{orderId}/accept", method= RequestMethod.POST)
  public ResponseEntity<String> accept(@RequestHeader(name = StaffAuthenticator.STAFF_TOKEN_HEADER, required = false) String staffToken,
                                       @PathVariable long orderId, @RequestBody OrderAcceptance orderAcceptance) {
    orderService.accept(orderId, orderAcceptance.getReadyBy(), authenticateStaff(staffToken));
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/{orderId}/preparing", method= RequestMethod.POST)
  public ResponseEntity<String> preparing(@RequestHeader(name = StaffAuthenticator.STAFF_TOKEN_HEADER, required = false) String staffToken,
                                          @PathVariable long orderId) {
    orderService.notePreparing(orderId, authenticateStaff(staffToken));
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/{orderId}/ready", method= RequestMethod.POST)
  public ResponseEntity<String> ready(@RequestHeader(name = StaffAuthenticator.STAFF_TOKEN_HEADER, required = false) String staffToken,
                                      @PathVariable long orderId) {
    orderService.noteReadyForPickup(orderId, authenticateStaff(staffToken));
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/{orderId}/pickedup", method= RequestMethod.POST)
  public ResponseEntity<String> pickedup(@RequestHeader(name = StaffAuthenticator.STAFF_TOKEN_HEADER, required = false) String staffToken,
                                         @PathVariable long orderId) {
    orderService.notePickedUp(orderId, authenticateStaff(staffToken));
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path="/{orderId}/delivered", method= RequestMethod.POST)
  public ResponseEntity<String> delivered(@RequestHeader(name = StaffAuthenticator.STAFF_TOKEN_HEADER, required = false) String staffToken,
                                          @PathVariable long orderId) {
    orderService.noteDelivered(orderId, authenticateStaff(staffToken));
    return new ResponseEntity<>(HttpStatus.OK);
  }

  private AuthenticatedStaff authenticateStaff(String staffToken) {
    return staffAuthenticator.authenticate(staffToken);
  }

}
