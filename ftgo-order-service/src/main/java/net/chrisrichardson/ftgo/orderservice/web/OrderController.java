package net.chrisrichardson.ftgo.orderservice.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.chrisrichardson.ftgo.domain.Action;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderRevision;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.GetOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "/api/v1/orders")
@Api(tags = "Orders", description = "Order management operations")
public class OrderController {

  private OrderService orderService;

  private OrderRepository orderRepository;


  public OrderController(OrderService orderService, OrderRepository orderRepository) {
    this.orderService = orderService;
    this.orderRepository = orderRepository;
  }

  @ApiOperation(value = "Create a new order", response = CreateOrderResponse.class)
  @ApiResponses({
      @ApiResponse(code = 200, message = "Order created successfully"),
      @ApiResponse(code = 400, message = "Invalid request")
  })
  @RequestMapping(method = RequestMethod.POST)
  public CreateOrderResponse create(@RequestBody CreateOrderRequest request) {
    Order order = orderService.createOrder(request.getConsumerId(),
            request.getRestaurantId(),
            request.getLineItems().stream().map(x -> new MenuItemIdAndQuantity(x.getMenuItemId(), x.getQuantity())).collect(toList())
    );
    return new CreateOrderResponse(order.getId());
  }

  @ApiOperation(value = "Get order by ID", response = GetOrderResponse.class)
  @ApiResponses({
      @ApiResponse(code = 200, message = "Order found"),
      @ApiResponse(code = 404, message = "Order not found")
  })
  @RequestMapping(path = "/{orderId}", method = RequestMethod.GET)
  public ResponseEntity<GetOrderResponse> getOrder(@ApiParam(value = "Order ID", required = true) @PathVariable long orderId) {
    Optional<Order> order = orderRepository.findById(orderId);
    return order.map(o -> new ResponseEntity<>(makeGetOrderResponse(o), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @ApiOperation(value = "Get orders by consumer ID", response = GetOrderResponse.class, responseContainer = "List")
  @ApiResponses({
      @ApiResponse(code = 200, message = "Orders retrieved successfully")
  })
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<GetOrderResponse>> getOrders(@ApiParam(value = "Consumer ID", required = true) @RequestParam long consumerId) {
    List<GetOrderResponse> orders = orderRepository.findAllByConsumerId(consumerId)
            .stream()
            .map(this::makeGetOrderResponse)
            .collect(Collectors.toList());

    return new ResponseEntity<>(orders, HttpStatus.OK);
  }

  private GetOrderResponse makeGetOrderResponse(Order order) {
    List<GetOrderResponse.CourierActionDTO> actionDTOs = Collections.emptyList();
    if (order.getAssignedCourier() != null) {
      List<Action> actions = order.getAssignedCourier().actionsForDelivery(order);
      if (actions != null) {
        actionDTOs = actions.stream()
            .map(a -> new GetOrderResponse.CourierActionDTO(
                a.getType().name(),
                null))
            .collect(toList());
      }
    }
    return new GetOrderResponse(order.getId(),
            order.getOrderState().name(),
            order.getOrderTotal().asString(),
            order.getRestaurant().getName(),
            order.getAssignedCourier() == null ? null : order.getAssignedCourier().getId(),
            actionDTOs
    );
  }

  @ApiOperation(value = "Cancel an order")
  @ApiResponses({
      @ApiResponse(code = 200, message = "Order cancelled successfully"),
      @ApiResponse(code = 404, message = "Order not found")
  })
  @RequestMapping(path = "/{orderId}/cancel", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> cancel(@ApiParam(value = "Order ID", required = true) @PathVariable long orderId) {
    try {
      Order order = orderService.cancel(orderId);
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @ApiOperation(value = "Revise an order")
  @ApiResponses({
      @ApiResponse(code = 200, message = "Order revised successfully"),
      @ApiResponse(code = 404, message = "Order not found")
  })
  @RequestMapping(path = "/{orderId}/revise", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> revise(@ApiParam(value = "Order ID", required = true) @PathVariable long orderId, @RequestBody ReviseOrderRequest request) {
    try {
      Order order = orderService.reviseOrder(orderId, new OrderRevision(Optional.empty(), request.getRevisedLineItemQuantities()));
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @ApiOperation(value = "Accept an order")
  @ApiResponses({
      @ApiResponse(code = 200, message = "Order accepted"),
      @ApiResponse(code = 404, message = "Order not found")
  })
  @RequestMapping(path="/{orderId}/accept", method= RequestMethod.POST)
  public ResponseEntity<String> accept(@ApiParam(value = "Order ID", required = true) @PathVariable long orderId, @RequestBody OrderAcceptance orderAcceptance) {
    orderService.accept(orderId, orderAcceptance.getReadyBy());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @ApiOperation(value = "Mark order as preparing")
  @ApiResponses({
      @ApiResponse(code = 200, message = "Order marked as preparing"),
      @ApiResponse(code = 404, message = "Order not found")
  })
  @RequestMapping(path="/{orderId}/preparing", method= RequestMethod.POST)
  public ResponseEntity<String> preparing(@ApiParam(value = "Order ID", required = true) @PathVariable long orderId) {
    orderService.notePreparing(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @ApiOperation(value = "Mark order as ready for pickup")
  @ApiResponses({
      @ApiResponse(code = 200, message = "Order marked as ready"),
      @ApiResponse(code = 404, message = "Order not found")
  })
  @RequestMapping(path="/{orderId}/ready", method= RequestMethod.POST)
  public ResponseEntity<String> ready(@ApiParam(value = "Order ID", required = true) @PathVariable long orderId) {
    orderService.noteReadyForPickup(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @ApiOperation(value = "Mark order as picked up")
  @ApiResponses({
      @ApiResponse(code = 200, message = "Order marked as picked up"),
      @ApiResponse(code = 404, message = "Order not found")
  })
  @RequestMapping(path="/{orderId}/pickedup", method= RequestMethod.POST)
  public ResponseEntity<String> pickedup(@ApiParam(value = "Order ID", required = true) @PathVariable long orderId) {
    orderService.notePickedUp(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @ApiOperation(value = "Mark order as delivered")
  @ApiResponses({
      @ApiResponse(code = 200, message = "Order marked as delivered"),
      @ApiResponse(code = 404, message = "Order not found")
  })
  @RequestMapping(path="/{orderId}/delivered", method= RequestMethod.POST)
  public ResponseEntity<String> delivered(@ApiParam(value = "Order ID", required = true) @PathVariable long orderId) {
    orderService.noteDelivered(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
