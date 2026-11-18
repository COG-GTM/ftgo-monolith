package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerResponse;
import net.chrisrichardson.ftgo.consumerservice.api.web.UpdateConsumerRequest;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/consumers")
public class ConsumerController {

  private static final Logger logger = LoggerFactory.getLogger(ConsumerController.class);

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

  @RequestMapping(method= RequestMethod.PUT,  path="/{consumerId}")
  public ResponseEntity<GetConsumerResponse> update(@PathVariable long consumerId, @RequestBody UpdateConsumerRequest request) {
    logger.info("Updating consumer {} with {}", consumerId, request);
    return consumerService.update(consumerId, request)
            .map(consumer -> new ResponseEntity<>(new GetConsumerResponse(consumer.getName()), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }
}
