package net.chrisrichardson.ftgo.restaurantservice.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantResponse;
import net.chrisrichardson.ftgo.restaurantservice.events.GetRestaurantResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/restaurants")
@Api(tags = "Restaurants", description = "Restaurant management operations")
public class RestaurantController {

  @Autowired
  private RestaurantService restaurantService;

  @ApiOperation(value = "Create a new restaurant", response = CreateRestaurantResponse.class)
  @ApiResponses({
      @ApiResponse(code = 200, message = "Restaurant created successfully"),
      @ApiResponse(code = 400, message = "Invalid request")
  })
  @RequestMapping(method = RequestMethod.POST)
  public CreateRestaurantResponse create(@RequestBody CreateRestaurantRequest request) {
    Restaurant r = restaurantService.create(request);
    return new CreateRestaurantResponse(r.getId());
  }

  @ApiOperation(value = "Get restaurant by ID", response = GetRestaurantResponse.class)
  @ApiResponses({
      @ApiResponse(code = 200, message = "Restaurant found"),
      @ApiResponse(code = 404, message = "Restaurant not found")
  })
  @RequestMapping(method = RequestMethod.GET, path = "/{restaurantId}")
  public ResponseEntity<GetRestaurantResponse> get(@ApiParam(value = "Restaurant ID", required = true) @PathVariable long restaurantId) {
    return restaurantService.findById(restaurantId)
            .map(r -> new ResponseEntity<>(new GetRestaurantResponse(r.getId(), r.getName()), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

}
