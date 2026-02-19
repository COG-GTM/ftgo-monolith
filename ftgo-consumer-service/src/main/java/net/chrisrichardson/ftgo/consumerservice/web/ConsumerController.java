package net.chrisrichardson.ftgo.consumerservice.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerResponse;
import net.chrisrichardson.ftgo.consumerservice.api.web.GetConsumerResponse;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/api/v1/consumers")
@Api(tags = "Consumers", description = "Consumer management operations")
public class ConsumerController {

  @Autowired
  private ConsumerService consumerService;

  @ApiOperation(value = "Create a new consumer", response = CreateConsumerResponse.class)
  @ApiResponses({
      @ApiResponse(code = 200, message = "Consumer created successfully"),
      @ApiResponse(code = 400, message = "Invalid request")
  })
  @RequestMapping(method= RequestMethod.POST)
  public CreateConsumerResponse create(@RequestBody CreateConsumerRequest request) {
    Consumer consumer = consumerService.create(request.getName());
    return new CreateConsumerResponse(consumer.getId());
  }

  @ApiOperation(value = "Get consumer by ID", response = GetConsumerResponse.class)
  @ApiResponses({
      @ApiResponse(code = 200, message = "Consumer found"),
      @ApiResponse(code = 404, message = "Consumer not found")
  })
  @RequestMapping(method= RequestMethod.GET,  path="/{consumerId}")
  public ResponseEntity<GetConsumerResponse> get(@ApiParam(value = "Consumer ID", required = true) @PathVariable long consumerId) {
    return consumerService.findById(consumerId)
            .map(consumer -> new ResponseEntity<>(new GetConsumerResponse(consumer.getId(), consumer.getName()), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }
}
