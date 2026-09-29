package net.chrisrichardson.ftgo.restaurantservice.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@Tag(name = "Restaurants")
public class RestaurantController {

  @Autowired
  private RestaurantService restaurantService;

  @Operation(summary = "Register a restaurant with its menu",
          requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Restaurant name, address and menu",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateRestaurantRequest.class),
                          examples = @ExampleObject(name = "Ajanta", value = RestaurantApiExamples.CREATE_RESTAURANT_REQUEST))),
          responses = @ApiResponse(responseCode = "200", description = "Restaurant created",
                  content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateRestaurantResponse.class),
                          examples = @ExampleObject(name = "Created", value = RestaurantApiExamples.CREATE_RESTAURANT_RESPONSE))))
  @RequestMapping(method = RequestMethod.POST)
  public CreateRestaurantResponse create(@RequestBody CreateRestaurantRequest request) {
    Restaurant r = restaurantService.create(request);
    return new CreateRestaurantResponse(r.getId());
  }

  @Operation(summary = "Get a restaurant",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Restaurant found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetRestaurantResponse.class),
                                  examples = @ExampleObject(name = "Ajanta", value = RestaurantApiExamples.GET_RESTAURANT_RESPONSE))),
                  @ApiResponse(responseCode = "404", description = "Restaurant not found (empty body)", content = @Content)
          })
  @RequestMapping(method = RequestMethod.GET, path = "/{restaurantId}")
  public ResponseEntity<GetRestaurantResponse> get(@Parameter(description = "Restaurant id", example = "1") @PathVariable long restaurantId) {
    return restaurantService.findById(restaurantId)
            .map(r -> new ResponseEntity<>(makeGetRestaurantResponse(r), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  private GetRestaurantResponse makeGetRestaurantResponse(Restaurant r) {
    return new GetRestaurantResponse(r.getId(), r.getName());
  }


}
