package net.chrisrichardson.ftgo.restaurantservice.web;

import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.restaurantservice.api.web.CreateRestaurantResponse;
import net.chrisrichardson.ftgo.restaurantservice.api.web.GetRestaurantResponse;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantService;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "/restaurants")
public class RestaurantController {

  private final RestaurantService restaurantService;

  public RestaurantController(RestaurantService restaurantService) {
    this.restaurantService = restaurantService;
  }

  @RequestMapping(method = RequestMethod.POST)
  public CreateRestaurantResponse create(@RequestBody CreateRestaurantRequest request) {
    Restaurant r = restaurantService.create(request);
    return new CreateRestaurantResponse(r.getId());
  }

  @RequestMapping(method = RequestMethod.GET, path = "/{restaurantId}")
  public ResponseEntity<GetRestaurantResponse> get(@PathVariable long restaurantId) {
    return restaurantService.findById(restaurantId)
            .map(r -> new ResponseEntity<>(makeGetRestaurantResponse(r), HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  private GetRestaurantResponse makeGetRestaurantResponse(Restaurant r) {
    return new GetRestaurantResponse(r.getId(), r.getName(), r.getAddress(), makeRestaurantMenuDTO(r.getMenuItems()));
  }

  private RestaurantMenuDTO makeRestaurantMenuDTO(List<MenuItem> menuItems) {
    return new RestaurantMenuDTO(menuItems.stream()
            .map(mi -> new MenuItemDTO(mi.getId(), mi.getName(), mi.getPrice()))
            .collect(toList()));
  }
}
