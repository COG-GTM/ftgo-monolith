package net.chrisrichardson.ftgo.orderservice.clients;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantMenu;
import net.chrisrichardson.ftgo.restaurantservice.api.web.GetRestaurantMenuItemResponse;
import net.chrisrichardson.ftgo.restaurantservice.api.web.GetRestaurantResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestaurantServiceClient {
    private final WebClient webClient;
    
    public RestaurantServiceClient(@Value("${restaurant.service.url}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }
    
    public Optional<Restaurant> findById(long restaurantId) {
        try {
            GetRestaurantResponse response = webClient.get()
                .uri("/restaurants/{restaurantId}", restaurantId)
                .retrieve()
                .bodyToMono(GetRestaurantResponse.class)
                .block();
            
            List<GetRestaurantMenuItemResponse> menuItemsResponse = webClient.get()
                .uri("/restaurants/{restaurantId}/menuitems", restaurantId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GetRestaurantMenuItemResponse>>() {})
                .block();
            
            if (response != null) {
                List<MenuItem> menuItems = menuItemsResponse != null ? 
                    menuItemsResponse.stream()
                        .map(mi -> new MenuItem(mi.getId(), mi.getName(), mi.getPrice()))
                        .collect(Collectors.toList()) : 
                    Collections.emptyList();
                
                Restaurant restaurant = new Restaurant(response.getId(), response.getName(), new RestaurantMenu(menuItems));
                return Optional.of(restaurant);
            }
            return Optional.empty();
        } catch (WebClientResponseException e) {
            if (e.getRawStatusCode() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }
}
