package net.chrisrichardson.ftgoconsumer;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerVerificationFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ConsumerOrderValidationController {
  private final ConsumerService consumerService;

  ConsumerOrderValidationController(ConsumerService consumerService) {
    this.consumerService = consumerService;
  }

  @PostMapping("/consumers/{consumerId}/validate-order")
  ResponseEntity<Void> validateOrder(@PathVariable long consumerId, @RequestBody ValidateOrderRequest request) {
    if (request.orderTotal() == null) {
      return ResponseEntity.badRequest().build();
    }
    try {
      consumerService.validateOrderForConsumer(consumerId, request.orderTotal());
      return ResponseEntity.noContent().build();
    } catch (ConsumerNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (ConsumerVerificationFailedException e) {
      return ResponseEntity.unprocessableEntity().build();
    }
  }

  record ValidateOrderRequest(Money orderTotal) {
  }
}
