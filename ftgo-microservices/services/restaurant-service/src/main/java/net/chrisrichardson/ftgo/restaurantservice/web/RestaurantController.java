package net.chrisrichardson.ftgo.restaurantservice.web;

import net.chrisrichardson.ftgo.restaurantservice.domain.Restaurant;
import net.chrisrichardson.ftgo.restaurantservice.domain.RestaurantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;

    public RestaurantController(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @PostMapping
    public ResponseEntity<Restaurant> create(@RequestBody Restaurant restaurant) {
        Restaurant saved = restaurantRepository.save(restaurant);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<Restaurant> get(@PathVariable long restaurantId) {
        Optional<Restaurant> restaurant = restaurantRepository.findById(restaurantId);
        return restaurant.map(r -> new ResponseEntity<>(r, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
