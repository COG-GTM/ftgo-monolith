package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerResponse;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path="/consumers")
public class ConsumerController {

  @Autowired
  private ConsumerService consumerService;

  @Autowired
  private OperatorApiKeyAuthorizer operatorApiKeyAuthorizer;

  public ConsumerController() {
  }

  public ConsumerController(ConsumerService consumerService, OperatorApiKeyAuthorizer operatorApiKeyAuthorizer) {
    this.consumerService = consumerService;
    this.operatorApiKeyAuthorizer = operatorApiKeyAuthorizer;
  }

  @RequestMapping(method= RequestMethod.POST)
  public CreateConsumerResponse create(@RequestBody CreateConsumerRequest request) {
    return new CreateConsumerResponse(consumerService.create(request.getName()).getId());
  }

  @RequestMapping(method= RequestMethod.GET,  path="/{consumerId}")
  public ResponseEntity<GetConsumerResponse> get(@PathVariable long consumerId, HttpServletRequest request) {
    if (!operatorApiKeyAuthorizer.isOperator(request)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
              .build();
    }
    return consumerService.findById(consumerId)
            .map(consumer -> new ResponseEntity<>(new GetConsumerResponse(consumer.getName()), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }
}
