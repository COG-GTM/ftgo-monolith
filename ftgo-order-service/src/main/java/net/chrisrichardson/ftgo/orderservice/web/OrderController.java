package net.chrisrichardson.ftgo.orderservice.web;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.NumberUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.time.LocalDateTime;
import java.util.Collections;
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

  @RequestMapping(method = RequestMethod.GET)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "consumerId", paramType = "query", dataType = "long", required = true),
      @ApiImplicitParam(name = "state", paramType = "query", dataType = "string", value = "Order state enum name"),
      @ApiImplicitParam(name = "page", paramType = "query", dataType = "long", value = "Zero-based page (default 0)"),
      @ApiImplicitParam(name = "size", paramType = "query", dataType = "int", value = "Page size (default 20, max 100)")
  })
  public ResponseEntity<List<GetOrderResponse>> getOrders(@ApiIgnore @RequestParam MultiValueMap<String, String> params) {
    long consumerId = NumberUtils.parseNumber(params.getFirst("consumerId"), Long.class);
    String stateParam = params.getFirst("state");
    OrderState state = stateParam == null ? null : OrderState.valueOf(stateParam);
    String pageParam = params.getFirst("page");
    String sizeParam = params.getFirst("size");
    List<Order> orders;
    if (pageParam == null && sizeParam == null) {
      orders = state == null ? orderRepository.findAllByConsumerId(consumerId)
              : orderRepository.findAllByConsumerIdAndOrderState(consumerId, state);
    } else {
      orders = findOrderPage(consumerId, state, pageParam, sizeParam);
    }
    List<GetOrderResponse> response = orders
            .stream()
            .map(this::makeGetOrderResponse)
            .collect(Collectors.toList());

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  private List<Order> findOrderPage(long consumerId, OrderState state, String pageParam, String sizeParam) {
    long page = pageParam == null ? 0 : Long.parseLong(pageParam);
    int size = sizeParam == null ? 20 : Integer.parseInt(sizeParam);
    if (page < 0 || size < 1 || size > 100) {
      throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100");
    }
    if (page > Integer.MAX_VALUE / size) {
      return Collections.emptyList();
    }

    Pageable pageable = PageRequest.of((int) page, size, Sort.Direction.ASC, "id");
    return state == null ? orderRepository.findAllByConsumerId(consumerId, pageable)
            : orderRepository.findAllByConsumerIdAndOrderState(consumerId, state, pageable);
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
