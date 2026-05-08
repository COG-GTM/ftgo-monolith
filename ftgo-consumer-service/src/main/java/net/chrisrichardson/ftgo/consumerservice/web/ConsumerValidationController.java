package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.consumerservice.api.web.ValidateOrderRequest;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsumerValidationController {

  @Autowired
  private ConsumerService consumerService;

  @PostMapping("/consumers/{consumerId}/validate")
  public ResponseEntity<?> validateOrder(@PathVariable long consumerId, @RequestBody ValidateOrderRequest request) {
    consumerService.validateOrderForConsumer(consumerId, request.getOrderTotal());
    return ResponseEntity.ok().build();
  }
}
