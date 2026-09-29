package net.chrisrichardson.ftgo.consumerservice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerResponse;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/consumers")
@Tag(name = "Consumers")
public class ConsumerController {

  @Autowired
  private ConsumerService consumerService;

  @Operation(summary = "Register a consumer",
          requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Consumer name",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateConsumerRequest.class),
                          examples = @ExampleObject(name = "John Doe", value = ConsumerApiExamples.CREATE_CONSUMER_REQUEST))),
          responses = @ApiResponse(responseCode = "200", description = "Consumer created",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateConsumerResponse.class),
                          examples = @ExampleObject(name = "Created", value = ConsumerApiExamples.CREATE_CONSUMER_RESPONSE))))
  @RequestMapping(method= RequestMethod.POST)
  public CreateConsumerResponse create(@RequestBody CreateConsumerRequest request) {
    return new CreateConsumerResponse(consumerService.create(request.getName()).getId());
  }

  @Operation(summary = "Get a consumer",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Consumer found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetConsumerResponse.class),
                                  examples = @ExampleObject(name = "John Doe", value = ConsumerApiExamples.GET_CONSUMER_RESPONSE))),
                  @ApiResponse(responseCode = "404", description = "Consumer not found (empty body)", content = @Content)
          })
  @RequestMapping(method= RequestMethod.GET,  path="/{consumerId}")
  public ResponseEntity<GetConsumerResponse> get(@Parameter(description = "Consumer id", example = "1") @PathVariable long consumerId) {
    return consumerService.findById(consumerId)
            .map(consumer -> new ResponseEntity<>(new GetConsumerResponse(consumer.getName()), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }
}
