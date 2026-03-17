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
import net.chrisrichardson.ftgo.openapi.model.ApiError;
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
@Tag(name = "Orders", description = "Order management operations")
public class OrderController {

  private OrderService orderService;

  private OrderRepository orderRepository;


  public OrderController(OrderService orderService, OrderRepository orderRepository) {
    this.orderService = orderService;
    this.orderRepository = orderRepository;
  }

  @Operation(summary = "Create a new order", description = "Creates a new order for a consumer at a restaurant")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Order created successfully",
                  content = @Content(schema = @Schema(implementation = CreateOrderResponse.class))),
          @ApiResponse(responseCode = "400", description = "Invalid request",
                  content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping
  public CreateOrderResponse create(@RequestBody CreateOrderRequest request) {
    Order order = orderService.createOrder(request.getConsumerId(),
            request.getRestaurantId(),
            request.getLineItems().stream().map(x -> new MenuItemIdAndQuantity(x.getMenuItemId(), x.getQuantity())).collect(toList())
    );
    return new CreateOrderResponse(order.getId());
  }


  @Operation(summary = "Get order by ID", description = "Retrieves an order by its unique identifier")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Order found",
                  content = @Content(schema = @Schema(implementation = GetOrderResponse.class))),
          @ApiResponse(responseCode = "404", description = "Order not found",
                  content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{orderId}")
  public ResponseEntity<GetOrderResponse> getOrder(
          @Parameter(description = "Order ID", required = true) @PathVariable long orderId) {
    Optional<Order> order = orderRepository.findById(orderId);
    return order.map(o -> new ResponseEntity<>(makeGetOrderResponse(o), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @Operation(summary = "Get orders by consumer", description = "Retrieves all orders for a given consumer")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
  })
  @GetMapping
  public ResponseEntity<List<GetOrderResponse>> getOrders(
          @Parameter(description = "Consumer ID", required = true) @RequestParam long consumerId) {
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

  @Operation(summary = "Cancel an order", description = "Cancels an existing order")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Order cancelled successfully",
                  content = @Content(schema = @Schema(implementation = GetOrderResponse.class))),
          @ApiResponse(responseCode = "404", description = "Order not found",
                  content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/{orderId}/cancel")
  public ResponseEntity<GetOrderResponse> cancel(
          @Parameter(description = "Order ID", required = true) @PathVariable long orderId) {
    try {
      Order order = orderService.cancel(orderId);
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @Operation(summary = "Revise an order", description = "Revises line item quantities of an existing order")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Order revised successfully",
                  content = @Content(schema = @Schema(implementation = GetOrderResponse.class))),
          @ApiResponse(responseCode = "404", description = "Order not found",
                  content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/{orderId}/revise")
  public ResponseEntity<GetOrderResponse> revise(
          @Parameter(description = "Order ID", required = true) @PathVariable long orderId,
          @RequestBody ReviseOrderRequest request) {
    try {
      Order order = orderService.reviseOrder(orderId, new OrderRevision(Optional.empty(), request.getRevisedLineItemQuantities()));
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @Operation(summary = "Accept an order", description = "Restaurant accepts the order with an estimated ready time")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Order accepted"),
          @ApiResponse(responseCode = "404", description = "Order not found",
                  content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/{orderId}/accept")
  public ResponseEntity<String> accept(
          @Parameter(description = "Order ID", required = true) @PathVariable long orderId,
          @RequestBody OrderAcceptance orderAcceptance) {
    orderService.accept(orderId, orderAcceptance.getReadyBy());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Mark order as preparing", description = "Marks the order as being prepared by the restaurant")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Order marked as preparing")
  })
  @PostMapping("/{orderId}/preparing")
  public ResponseEntity<String> preparing(
          @Parameter(description = "Order ID", required = true) @PathVariable long orderId) {
    orderService.notePreparing(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Mark order as ready", description = "Marks the order as ready for pickup")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Order marked as ready for pickup")
  })
  @PostMapping("/{orderId}/ready")
  public ResponseEntity<String> ready(
          @Parameter(description = "Order ID", required = true) @PathVariable long orderId) {
    orderService.noteReadyForPickup(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Mark order as picked up", description = "Marks the order as picked up by the courier")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Order marked as picked up")
  })
  @PostMapping("/{orderId}/pickedup")
  public ResponseEntity<String> pickedup(
          @Parameter(description = "Order ID", required = true) @PathVariable long orderId) {
    orderService.notePickedUp(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Mark order as delivered", description = "Marks the order as delivered to the consumer")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Order marked as delivered")
  })
  @PostMapping("/{orderId}/delivered")
  public ResponseEntity<String> delivered(
          @Parameter(description = "Order ID", required = true) @PathVariable long orderId) {
    orderService.noteDelivered(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
