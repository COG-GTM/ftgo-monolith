package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.orderservice.api.OrderNotFoundException;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/orders")
public class OrderProxyController {

  private final OrderServiceProxy orderServiceProxy;

  public OrderProxyController(OrderServiceProxy orderServiceProxy) {
    this.orderServiceProxy = orderServiceProxy;
  }

  @RequestMapping(method = RequestMethod.POST)
  public CreateOrderResponse create(@RequestBody CreateOrderRequest request) {
    return orderServiceProxy.createOrder(request);
  }

  @RequestMapping(path = "/{orderId}", method = RequestMethod.GET)
  public ResponseEntity<Map> getOrder(@PathVariable long orderId) {
    try {
      Map response = orderServiceProxy.getOrder(orderId);
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List> getOrders(@RequestParam long consumerId) {
    List response = orderServiceProxy.getOrders(consumerId);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @RequestMapping(path = "/{orderId}/cancel", method = RequestMethod.POST)
  public ResponseEntity<Map> cancel(@PathVariable long orderId) {
    try {
      Map response = orderServiceProxy.cancelOrder(orderId);
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(path = "/{orderId}/revise", method = RequestMethod.POST)
  public ResponseEntity<Map> revise(@PathVariable long orderId, @RequestBody ReviseOrderRequest request) {
    try {
      Map response = orderServiceProxy.reviseOrder(orderId, request);
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (OrderNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(path = "/{orderId}/accept", method = RequestMethod.POST)
  public ResponseEntity<String> accept(@PathVariable long orderId, @RequestBody OrderAcceptance orderAcceptance) {
    orderServiceProxy.acceptOrder(orderId, orderAcceptance);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path = "/{orderId}/preparing", method = RequestMethod.POST)
  public ResponseEntity<String> preparing(@PathVariable long orderId) {
    orderServiceProxy.notePreparing(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path = "/{orderId}/ready", method = RequestMethod.POST)
  public ResponseEntity<String> ready(@PathVariable long orderId) {
    orderServiceProxy.noteReadyForPickup(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path = "/{orderId}/pickedup", method = RequestMethod.POST)
  public ResponseEntity<String> pickedup(@PathVariable long orderId) {
    orderServiceProxy.notePickedUp(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @RequestMapping(path = "/{orderId}/delivered", method = RequestMethod.POST)
  public ResponseEntity<String> delivered(@PathVariable long orderId) {
    orderServiceProxy.noteDelivered(orderId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
