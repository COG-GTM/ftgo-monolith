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
import net.chrisrichardson.ftgo.openapi.model.ApiError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/consumers")
@Tag(name = "Consumers", description = "Consumer management operations")
public class ConsumerController {

  @Autowired
  private ConsumerService consumerService;

  @Operation(summary = "Create a new consumer", description = "Registers a new consumer in the system")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Consumer created successfully",
                  content = @Content(schema = @Schema(implementation = CreateConsumerResponse.class))),
          @ApiResponse(responseCode = "400", description = "Invalid request",
                  content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping
  public CreateConsumerResponse create(@RequestBody CreateConsumerRequest request) {
    return new CreateConsumerResponse(consumerService.create(request.getName()).getId());
  }

  @Operation(summary = "Get consumer by ID", description = "Retrieves a consumer by their unique identifier")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Consumer found",
                  content = @Content(schema = @Schema(implementation = GetConsumerResponse.class))),
          @ApiResponse(responseCode = "404", description = "Consumer not found",
                  content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{consumerId}")
  public ResponseEntity<GetConsumerResponse> get(
          @Parameter(description = "Consumer ID", required = true) @PathVariable long consumerId) {
    return consumerService.findById(consumerId)
            .map(consumer -> new ResponseEntity<>(new GetConsumerResponse(consumer.getName()), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }
}
