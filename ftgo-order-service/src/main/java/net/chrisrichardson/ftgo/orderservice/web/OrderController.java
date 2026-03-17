package net.chrisrichardson.ftgo.orderservice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderRevision;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "/orders")
@Tag(name = "Orders", description = "Order management API - create, retrieve, and manage order lifecycle")
public class OrderController {

  private OrderService orderService;

  private OrderRepository orderRepository;


  public OrderController(OrderService orderService, OrderRepository orderRepository) {
    this.orderService = orderService;
    this.orderRepository = orderRepository;
  }

  @Operation(summary = "Create a new order", description = "Creates a new order for the specified consumer and restaurant")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order created successfully",
          content = @Content(schema = @Schema(implementation = CreateOrderResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid order request"),
      @ApiResponse(responseCode = "404", description = "Consumer or restaurant not found")
  })
  @RequestMapping(method = RequestMethod.POST)
  public CreateOrderResponse create(@RequestBody CreateOrderRequest request) {
    Order order = orderService.createOrder(request.getConsumerId(),
            request.getRestaurantId(),
            request.getLineItems().stream().map(x -> new MenuItemIdAndQuantity(x.getMenuItemId(), x.getQuantity())).collect(toList())
    );
    return new CreateOrderResponse(order.getId());
  }


  @Operation(summary = "Get order by ID", description = "Retrieves a specific order by its unique identifier")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order found",
          content = @Content(schema = @Schema(implementation = GetOrderResponse.class))),
      @ApiResponse(responseCode = "404", description = "Order not found")
  })
  @RequestMapping(path = "/{orderId}", method = RequestMethod.GET)
  public ResponseEntity<GetOrderResponse> getOrder(
      @Parameter(description = "Unique order identifier", required = true) @PathVariable long orderId) {
    Optional<Order> order = orderRepository.findById(orderId);
    return order.map(o -> new ResponseEntity<>(makeGetOrderResponse(o), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @Operation(summary = "Get orders by consumer", description = "Retrieves all orders for a specific consumer")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
  })
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<GetOrderResponse>> getOrders(
      @Parameter(description = "Consumer identifier to filter orders", required = true) @RequestParam long consumerId) {
    List<GetOrderResponse> orders = orderRepository.findAllByConsumerId(consumerId)
            .stream()
            .map(this::makeGetOrderResponse)
            .collect(Collectors.toList());

    return new ResponseEntity<>(orders, HttpStatus.OK);
  }

  private GetOrderResponse makeGetOrderResponse(Order order) {
    return new GetOrderResponse(order.getId(),
            order.getOrderState().name(),
            order.getOrderTotal(),
            order.getRestaurant().getName(),
            order.getAssignedCourier() == null ? null : order.getAssignedCourier().getId(),
            order.getAssignedCourier() == null ? null : order.getAssignedCourier().actionsForDelivery(order)
    );
  }

  @Operation(summary = "Cancel an order", description = "Cancels an existing order if it is in a cancellable state")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
      @ApiResponse(responseCode = "404", description = "Order not found")
  })
  @RequestMapping(path = "/{orderId}/cancel", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> cancel(
      @Parameter(description = "Order identifier", required = true) @PathVariable long orderId) {
    try {
      Order order = orderService.cancel(orderId);
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @Operation(summary = "Revise an order", description = "Updates the line item quantities of an existing order")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order revised successfully"),
      @ApiResponse(responseCode = "404", description = "Order not found")
  })
  @RequestMapping(path = "/{orderId}/revise", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> revise(
      @Parameter(description = "Order identifier", required = true) @PathVariable long orderId,
      @RequestBody ReviseOrderRequest request) {
    try {
      Order order = orderService.reviseOrder(orderId, new OrderRevision(Optional.empty(), request.getRevisedLineItemQuantities()));
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @Operation(summary = "Accept an order", description = "Restaurant accepts the order and provides an estimated ready time")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Order accepted"),
      @ApiResponse(responseCode = "404", description = "Order not found")
  })
  @RequestMapping(path="/{orderId}/accept", method= RequestMethod.POST)
  public ResponseEntity<String> accept(
      @Parameter(description = "Order identifier", required = true) @PathVariable long orderId,
      @RequestBody OrderAcceptance orderAcceptance) {
    orderService.accept(orderId, orderAcceptance.getReadyBy());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Mark order as preparing", description = "Marks that the restaurant has started preparing the order")
  @ApiResponse(responseCode = "200", description = "Order marked as preparing")
  @RequestMapping(path="/{orderId}/preparing", method= RequestMethod.POST)
  public ResponseEntity<String> preparing(
      @Parameter(description = "Order identifier", required = true) @PathVariable long orderId) {
    orderService.notePreparing(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Mark order as ready", description = "Marks that the order is ready for pickup by courier")
  @ApiResponse(responseCode = "200", description = "Order marked as ready for pickup")
  @RequestMapping(path="/{orderId}/ready", method= RequestMethod.POST)
  public ResponseEntity<String> ready(
      @Parameter(description = "Order identifier", required = true) @PathVariable long orderId) {
    orderService.noteReadyForPickup(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Mark order as picked up", description = "Marks that the courier has picked up the order")
  @ApiResponse(responseCode = "200", description = "Order marked as picked up")
  @RequestMapping(path="/{orderId}/pickedup", method= RequestMethod.POST)
  public ResponseEntity<String> pickedup(
      @Parameter(description = "Order identifier", required = true) @PathVariable long orderId) {
    orderService.notePickedUp(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Mark order as delivered", description = "Marks that the order has been delivered to the consumer")
  @ApiResponse(responseCode = "200", description = "Order marked as delivered")
  @RequestMapping(path="/{orderId}/delivered", method= RequestMethod.POST)
  public ResponseEntity<String> delivered(
      @Parameter(description = "Order identifier", required = true) @PathVariable long orderId) {
    orderService.noteDelivered(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
