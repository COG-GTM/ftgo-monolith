package net.chrisrichardson.ftgo.consumerservice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Consumers", description = "Consumer management API - register and retrieve consumers")
public class ConsumerController {

  @Autowired
  private ConsumerService consumerService;

  @Operation(summary = "Register a new consumer", description = "Creates a new consumer account in the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Consumer created successfully",
          content = @Content(schema = @Schema(implementation = CreateConsumerResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid consumer data")
  })
  @RequestMapping(method= RequestMethod.POST)
  public CreateConsumerResponse create(@RequestBody CreateConsumerRequest request) {
    return new CreateConsumerResponse(consumerService.create(request.getName()).getId());
  }

  @Operation(summary = "Get consumer by ID", description = "Retrieves a specific consumer by their unique identifier")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Consumer found",
          content = @Content(schema = @Schema(implementation = GetConsumerResponse.class))),
      @ApiResponse(responseCode = "404", description = "Consumer not found")
  })
  @RequestMapping(method= RequestMethod.GET,  path="/{consumerId}")
  public ResponseEntity<GetConsumerResponse> get(
      @Parameter(description = "Unique consumer identifier", required = true) @PathVariable long consumerId) {
    return consumerService.findById(consumerId)
            .map(consumer -> new ResponseEntity<>(new GetConsumerResponse(consumer.getName()), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }
}
