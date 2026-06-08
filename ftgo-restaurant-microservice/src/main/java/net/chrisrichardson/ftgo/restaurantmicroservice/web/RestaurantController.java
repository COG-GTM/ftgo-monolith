package net.chrisrichardson.ftgo.restaurantmicroservice.web;

import net.chrisrichardson.ftgo.restaurantmicroservice.domain.Restaurant;
import net.chrisrichardson.ftgo.restaurantmicroservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.ReviseMenuRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/restaurants")
public class RestaurantController {

  @Autowired
  private RestaurantService restaurantService;

  @RequestMapping(method = RequestMethod.POST)
  public CreateRestaurantResponse create(@RequestBody CreateRestaurantRequest request) {
    Restaurant restaurant = restaurantService.create(request);
    return new CreateRestaurantResponse(restaurant.getId());
  }

  @RequestMapping(method = RequestMethod.GET, path = "/{restaurantId}")
  public ResponseEntity<RestaurantDTO> get(@PathVariable long restaurantId) {
    return restaurantService.findById(restaurantId)
            .map(r -> new ResponseEntity<>(RestaurantMapper.toRestaurantDTO(r), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @RequestMapping(method = RequestMethod.PUT, path = "/{restaurantId}/menu")
  public ResponseEntity<RestaurantDTO> reviseMenu(@PathVariable long restaurantId,
                                                  @RequestBody ReviseMenuRequest request) {
    Restaurant restaurant = restaurantService.reviseMenu(restaurantId, request.getMenu());
    return new ResponseEntity<>(RestaurantMapper.toRestaurantDTO(restaurant), HttpStatus.OK);
  }
}
