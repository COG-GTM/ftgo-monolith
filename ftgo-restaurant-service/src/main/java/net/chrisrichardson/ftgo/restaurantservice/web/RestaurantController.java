package net.chrisrichardson.ftgo.restaurantservice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/restaurants")
@Tag(name = "Restaurants", description = "Restaurant management API - register and retrieve restaurants")
public class RestaurantController {

  @Autowired
  private RestaurantService restaurantService;

  @Operation(summary = "Register a new restaurant", description = "Creates a new restaurant with its menu in the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Restaurant created successfully",
          content = @Content(schema = @Schema(implementation = CreateRestaurantResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid restaurant data")
  })
  @RequestMapping(method = RequestMethod.POST)
  public CreateRestaurantResponse create(@RequestBody CreateRestaurantRequest request) {
    Restaurant r = restaurantService.create(request);
    return new CreateRestaurantResponse(r.getId());
  }

  @Operation(summary = "Get restaurant by ID", description = "Retrieves a specific restaurant by its unique identifier")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Restaurant found",
          content = @Content(schema = @Schema(implementation = GetRestaurantResponse.class))),
      @ApiResponse(responseCode = "404", description = "Restaurant not found")
  })
  @RequestMapping(method = RequestMethod.GET, path = "/{restaurantId}")
  public ResponseEntity<GetRestaurantResponse> get(
      @Parameter(description = "Unique restaurant identifier", required = true) @PathVariable long restaurantId) {
    return restaurantService.findById(restaurantId)
            .map(r -> new ResponseEntity<>(makeGetRestaurantResponse(r), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  private GetRestaurantResponse makeGetRestaurantResponse(Restaurant r) {
    return new GetRestaurantResponse(r.getId(), r.getName());
  }


}
