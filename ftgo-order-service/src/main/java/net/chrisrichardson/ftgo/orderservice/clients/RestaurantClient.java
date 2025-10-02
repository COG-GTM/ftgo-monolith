package net.chrisrichardson.ftgo.orderservice.clients;

import net.chrisrichardson.ftgo.domain.Restaurant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class RestaurantClient {
    private final RestTemplate restTemplate;
    private final String restaurantServiceUrl;

    public RestaurantClient(RestTemplate restTemplate,
                           @Value("${restaurant.service.url:http://localhost:8082}") String restaurantServiceUrl) {
        this.restTemplate = restTemplate;
        this.restaurantServiceUrl = restaurantServiceUrl;
    }

    public Optional<Restaurant> findById(long restaurantId) {
        try {
            ResponseEntity<Restaurant> response = restTemplate.getForEntity(
                restaurantServiceUrl + "/restaurants/" + restaurantId,
                Restaurant.class
            );
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }
}
