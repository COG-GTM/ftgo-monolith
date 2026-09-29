package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.domain.*;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.chrisrichardson.eventstore.examples.customersandorders.commonswagger.ErrorResponseExamples;
import net.chrisrichardson.ftgo.common.ErrorResponse;
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
@Tag(name = "Orders")
public class OrderController {

  private OrderService orderService;

  private OrderRepository orderRepository;


  public OrderController(OrderService orderService, OrderRepository orderRepository) {
    this.orderService = orderService;
    this.orderRepository = orderRepository;
  }

  @Operation(summary = "Place an order",
          description = "Creates an order in the APPROVED state for the given consumer and restaurant.",
          requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Consumer, restaurant and the menu items to order",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateOrderRequest.class),
                          examples = @ExampleObject(name = "Two line items", value = OrderApiExamples.CREATE_ORDER_REQUEST))),
          responses = {
                  @ApiResponse(responseCode = "200", description = "Order created",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateOrderResponse.class),
                                  examples = @ExampleObject(name = "Created", value = OrderApiExamples.CREATE_ORDER_RESPONSE))),
                  @ApiResponse(responseCode = "404", description = "Restaurant not found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Restaurant not found", value = ErrorResponseExamples.RESTAURANT_NOT_FOUND))),
                  @ApiResponse(responseCode = "500", description = "Unknown consumer or menu item",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Internal error", value = ErrorResponseExamples.INTERNAL_SERVER_ERROR)))
          })
  @RequestMapping(method = RequestMethod.POST)
  public CreateOrderResponse create(@RequestBody CreateOrderRequest request) {
    Order order = orderService.createOrder(request.getConsumerId(),
            request.getRestaurantId(),
            request.getLineItems().stream().map(x -> new MenuItemIdAndQuantity(x.getMenuItemId(), x.getQuantity())).collect(toList())
    );
    return new CreateOrderResponse(order.getId());
  }


  @Operation(summary = "Get an order",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Order found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetOrderResponse.class),
                                  examples = {
                                          @ExampleObject(name = "Accepted order with courier", value = OrderApiExamples.GET_ORDER_RESPONSE),
                                          @ExampleObject(name = "Newly approved order", value = OrderApiExamples.GET_APPROVED_ORDER_RESPONSE)
                                  })),
                  @ApiResponse(responseCode = "404", description = "Order not found (empty body)", content = @Content)
          })
  @RequestMapping(path = "/{orderId}", method = RequestMethod.GET)
  public ResponseEntity<GetOrderResponse> getOrder(@Parameter(description = "Order id", example = "1") @PathVariable long orderId) {
    Optional<Order> order = orderRepository.findById(orderId);
    return order.map(o -> new ResponseEntity<>(makeGetOrderResponse(o), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @Operation(summary = "List a consumer's orders",
          responses = @ApiResponse(responseCode = "200", description = "Orders placed by the consumer (possibly empty)",
                  content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GetOrderResponse.class)),
                          examples = @ExampleObject(name = "Two orders", value = OrderApiExamples.GET_ORDERS_RESPONSE))))
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<GetOrderResponse>> getOrders(@Parameter(description = "Consumer id", example = "1") @RequestParam long consumerId) {
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

  @Operation(summary = "Cancel an order",
          description = "Cancels an order. Only APPROVED orders can be cancelled.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Order cancelled",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetOrderResponse.class),
                                  examples = @ExampleObject(name = "Cancelled", value = OrderApiExamples.GET_CANCELLED_ORDER_RESPONSE))),
                  @ApiResponse(responseCode = "404", description = "Order not found (empty body)", content = @Content),
                  @ApiResponse(responseCode = "409", description = "Order cannot be cancelled in its current state",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Already delivered", value = ErrorResponseExamples.UNSUPPORTED_STATE_TRANSITION)))
          })
  @RequestMapping(path = "/{orderId}/cancel", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> cancel(@Parameter(description = "Order id", example = "1") @PathVariable long orderId) {
    try {
      Order order = orderService.cancel(orderId);
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @Operation(summary = "Revise line item quantities",
          description = "Sets new quantities for the order's line items, keyed by menu item id. Every line item of the order must be listed; omitted items cause a 500.",
          requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "New quantity per menu item id",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReviseOrderRequest.class),
                          examples = @ExampleObject(name = "Reduce item 1 to 2, keep item 2", value = OrderApiExamples.REVISE_ORDER_REQUEST))),
          responses = {
                  @ApiResponse(responseCode = "200", description = "Order revised",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetOrderResponse.class),
                                  examples = @ExampleObject(name = "Revised", value = OrderApiExamples.GET_REVISED_ORDER_RESPONSE))),
                  @ApiResponse(responseCode = "404", description = "Order not found (empty body)", content = @Content),
                  @ApiResponse(responseCode = "409", description = "Order cannot be revised in its current state",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Already delivered", value = ErrorResponseExamples.UNSUPPORTED_STATE_TRANSITION)))
          })
  @RequestMapping(path = "/{orderId}/revise", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> revise(@Parameter(description = "Order id", example = "1") @PathVariable long orderId, @RequestBody ReviseOrderRequest request) {
    try {
      Order order = orderService.reviseOrder(orderId, new OrderRevision(Optional.empty(), request.getRevisedLineItemQuantities()));
      return new ResponseEntity<>(makeGetOrderResponse(order), HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @Operation(summary = "Accept an order",
          description = "Restaurant accepts an APPROVED order, promising it will be ready by `readyBy`; a courier is assigned.",
          requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Time by which the restaurant promises the order will be ready",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderAcceptance.class),
                          examples = @ExampleObject(name = "Ready in one hour", value = OrderApiExamples.ORDER_ACCEPTANCE_REQUEST))),
          responses = {
                  @ApiResponse(responseCode = "200", description = "Order accepted (empty body)", content = @Content),
                  @ApiResponse(responseCode = "400", description = "readyBy is not in the future",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "readyBy in the past", value = ErrorResponseExamples.BAD_REQUEST))),
                  @ApiResponse(responseCode = "404", description = "Order not found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Order not found", value = ErrorResponseExamples.ORDER_NOT_FOUND))),
                  @ApiResponse(responseCode = "409", description = "Order is not APPROVED",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Invalid transition", value = ErrorResponseExamples.UNSUPPORTED_STATE_TRANSITION))),
                  @ApiResponse(responseCode = "503", description = "No courier available",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "No courier", value = ErrorResponseExamples.NO_COURIER_AVAILABLE)))
          })
  @RequestMapping(path="/{orderId}/accept", method= RequestMethod.POST)
  public ResponseEntity<String> accept(@Parameter(description = "Order id", example = "1") @PathVariable long orderId, @RequestBody OrderAcceptance orderAcceptance) {
    orderService.accept(orderId, orderAcceptance.getReadyBy());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Mark an order as being prepared",
          description = "Valid only when the order is ACCEPTED.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "State updated (empty body)", content = @Content),
                  @ApiResponse(responseCode = "404", description = "Order not found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Order not found", value = ErrorResponseExamples.ORDER_NOT_FOUND))),
                  @ApiResponse(responseCode = "409", description = "Order is not ACCEPTED",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Invalid transition", value = ErrorResponseExamples.UNSUPPORTED_STATE_TRANSITION)))
          })
  @RequestMapping(path="/{orderId}/preparing", method= RequestMethod.POST)
  public ResponseEntity<String> preparing(@Parameter(description = "Order id", example = "1") @PathVariable long orderId) {
    orderService.notePreparing(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Mark an order as ready for pickup",
          description = "Valid only when the order is PREPARING.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "State updated (empty body)", content = @Content),
                  @ApiResponse(responseCode = "404", description = "Order not found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Order not found", value = ErrorResponseExamples.ORDER_NOT_FOUND))),
                  @ApiResponse(responseCode = "409", description = "Order is not PREPARING",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Invalid transition", value = ErrorResponseExamples.UNSUPPORTED_STATE_TRANSITION)))
          })
  @RequestMapping(path="/{orderId}/ready", method= RequestMethod.POST)
  public ResponseEntity<String> ready(@Parameter(description = "Order id", example = "1") @PathVariable long orderId) {
    orderService.noteReadyForPickup(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Mark an order as picked up by the courier",
          description = "Valid only when the order is READY_FOR_PICKUP.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "State updated (empty body)", content = @Content),
                  @ApiResponse(responseCode = "404", description = "Order not found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Order not found", value = ErrorResponseExamples.ORDER_NOT_FOUND))),
                  @ApiResponse(responseCode = "409", description = "Order is not READY_FOR_PICKUP",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Invalid transition", value = ErrorResponseExamples.UNSUPPORTED_STATE_TRANSITION)))
          })
  @RequestMapping(path="/{orderId}/pickedup", method= RequestMethod.POST)
  public ResponseEntity<String> pickedup(@Parameter(description = "Order id", example = "1") @PathVariable long orderId) {
    orderService.notePickedUp(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Operation(summary = "Mark an order as delivered",
          description = "Valid only when the order is PICKED_UP.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "State updated (empty body)", content = @Content),
                  @ApiResponse(responseCode = "404", description = "Order not found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Order not found", value = ErrorResponseExamples.ORDER_NOT_FOUND))),
                  @ApiResponse(responseCode = "409", description = "Order is not PICKED_UP",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                                  examples = @ExampleObject(name = "Invalid transition", value = ErrorResponseExamples.UNSUPPORTED_STATE_TRANSITION)))
          })
  @RequestMapping(path="/{orderId}/delivered", method= RequestMethod.POST)
  public ResponseEntity<String> delivered(@Parameter(description = "Order id", example = "1") @PathVariable long orderId) {
    orderService.noteDelivered(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
