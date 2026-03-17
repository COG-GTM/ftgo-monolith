package net.chrisrichardson.ftgo.restaurantservice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.openapi.model.ApiError;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/restaurants")
@Tag(name = "Restaurants", description = "Restaurant management operations")
public class RestaurantController {

  @Autowired
  private RestaurantService restaurantService;

  @Operation(summary = "Create a new restaurant", description = "Registers a new restaurant with its menu")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Restaurant created successfully",
                  content = @Content(schema = @Schema(implementation = CreateRestaurantResponse.class))),
          @ApiResponse(responseCode = "400", description = "Invalid request",
                  content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping
  public CreateRestaurantResponse create(@RequestBody CreateRestaurantRequest request) {
    Restaurant r = restaurantService.create(request);
    return new CreateRestaurantResponse(r.getId());
  }

  @Operation(summary = "Get restaurant by ID", description = "Retrieves a restaurant by its unique identifier")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Restaurant found",
                  content = @Content(schema = @Schema(implementation = GetRestaurantResponse.class))),
          @ApiResponse(responseCode = "404", description = "Restaurant not found",
                  content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{restaurantId}")
  public ResponseEntity<GetRestaurantResponse> get(
          @Parameter(description = "Restaurant ID", required = true) @PathVariable long restaurantId) {
    return restaurantService.findById(restaurantId)
            .map(r -> new ResponseEntity<>(makeGetRestaurantResponse(r), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  private GetRestaurantResponse makeGetRestaurantResponse(Restaurant r) {
    return new GetRestaurantResponse(r.getId(), r.getName());
  }


}
