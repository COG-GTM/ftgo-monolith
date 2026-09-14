package net.chrisrichardson.ftgo.orders;

import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.GetOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import net.chrisrichardson.ftgo.orderservice.client.OrderServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/orders")
public class OrderProxyController {

  private final OrderServiceClient orderServiceClient;

  public OrderProxyController(OrderServiceClient orderServiceClient) {
    this.orderServiceClient = orderServiceClient;
  }

  @RequestMapping(method = RequestMethod.POST)
  public CreateOrderResponse create(@RequestBody CreateOrderRequest request) {
    return orderServiceClient.createOrder(request);
  }

  @RequestMapping(path = "/{orderId}", method = RequestMethod.GET)
  public ResponseEntity<GetOrderResponse> getOrder(@PathVariable long orderId) {
    return orderServiceClient.getOrder(orderId)
            .map(response -> new ResponseEntity<>(response, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<GetOrderResponse>> getOrders(@RequestParam long consumerId) {
    return new ResponseEntity<>(orderServiceClient.getOrdersForConsumer(consumerId), HttpStatus.OK);
  }

  @RequestMapping(path = "/{orderId}/cancel", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> cancel(@PathVariable long orderId) {
    return orderServiceClient.cancel(orderId)
            .map(response -> new ResponseEntity<>(response, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @RequestMapping(path = "/{orderId}/revise", method = RequestMethod.POST)
  public ResponseEntity<GetOrderResponse> revise(@PathVariable long orderId,
                                                  @RequestBody ReviseOrderRequest request) {
    return orderServiceClient.revise(orderId, request)
            .map(response -> new ResponseEntity<>(response, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @RequestMapping(path = "/{orderId}/accept", method = RequestMethod.POST)
  public ResponseEntity<String> accept(@PathVariable long orderId,
                                       @RequestBody OrderAcceptance orderAcceptance) {
    orderServiceClient.accept(orderId, orderAcceptance);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path = "/{orderId}/preparing", method = RequestMethod.POST)
  public ResponseEntity<String> preparing(@PathVariable long orderId) {
    orderServiceClient.notePreparing(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path = "/{orderId}/ready", method = RequestMethod.POST)
  public ResponseEntity<String> ready(@PathVariable long orderId) {
    orderServiceClient.noteReadyForPickup(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path = "/{orderId}/pickedup", method = RequestMethod.POST)
  public ResponseEntity<String> pickedup(@PathVariable long orderId) {
    orderServiceClient.notePickedUp(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path = "/{orderId}/delivered", method = RequestMethod.POST)
  public ResponseEntity<String> delivered(@PathVariable long orderId) {
    orderServiceClient.noteDelivered(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
