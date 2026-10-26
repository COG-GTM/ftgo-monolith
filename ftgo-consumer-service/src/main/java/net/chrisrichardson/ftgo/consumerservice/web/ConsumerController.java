package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerResponse;
import net.chrisrichardson.ftgo.consumerservice.api.web.ValidateOrderByConsumerRequest;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerNotFoundException;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerVerificationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/consumers")
public class ConsumerController {

  @Autowired
  private ConsumerService consumerService;

  @RequestMapping(method= RequestMethod.POST)
  public CreateConsumerResponse create(@RequestBody CreateConsumerRequest request) {
    return new CreateConsumerResponse(consumerService.create(request.getName()).getId());
  }

  @RequestMapping(method= RequestMethod.GET,  path="/{consumerId}")
  public ResponseEntity<GetConsumerResponse> get(@PathVariable long consumerId) {
    return consumerService.findById(consumerId)
            .map(consumer -> new ResponseEntity<>(new GetConsumerResponse(consumer.getName()), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @RequestMapping(method= RequestMethod.POST, path="/{consumerId}/validate")
  @ResponseStatus(HttpStatus.OK)
  public void validate(@PathVariable long consumerId, @RequestBody ValidateOrderByConsumerRequest request) {
    consumerService.validateOrderForConsumer(consumerId, request.getOrderTotal());
  }

  @ExceptionHandler(ConsumerNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public void consumerNotFound() {
  }

  @ExceptionHandler(ConsumerVerificationFailedException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public void consumerVerificationFailed() {
  }
}
