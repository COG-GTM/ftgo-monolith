package net.chrisrichardson.ftgo.orderservice.client;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.restaurantservice.api.GetRestaurantWithMenuResponse;
import net.chrisrichardson.ftgo.restaurantservice.api.InvalidMenuItemIdException;
import net.chrisrichardson.ftgo.restaurantservice.api.RestaurantNotFoundException;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

public class RestaurantServiceProxy {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RestTemplate restTemplate;
    private final String restaurantServiceUrl;

    public RestaurantServiceProxy(RestTemplate restTemplate, String restaurantServiceUrl) {
        this.restTemplate = restTemplate;
        this.restaurantServiceUrl = restaurantServiceUrl;
    }

    public GetRestaurantWithMenuResponse findRestaurantWithMenu(long restaurantId) {
        String url = restaurantServiceUrl + "/restaurants/" + restaurantId + "/with-menu";
        logger.debug("Fetching restaurant with menu from: {}", url);

        try {
            ResponseEntity<GetRestaurantWithMenuResponse> response =
                    restTemplate.getForEntity(url, GetRestaurantWithMenuResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.debug("Successfully fetched restaurant: {}", restaurantId);
                return response.getBody();
            }
            throw new RestaurantNotFoundException(restaurantId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.warn("Restaurant not found: {}", restaurantId);
                throw new RestaurantNotFoundException(restaurantId);
            }
            logger.error("Error fetching restaurant {}: {}", restaurantId, e.getMessage());
            throw e;
        }
    }

    public Optional<MenuItemDTO> findMenuItem(long restaurantId, String menuItemId) {
        GetRestaurantWithMenuResponse restaurant = findRestaurantWithMenu(restaurantId);
        if (restaurant.getMenu() == null || restaurant.getMenu().getMenuItemDTOs() == null) {
            return Optional.empty();
        }
        return restaurant.getMenu().getMenuItemDTOs().stream()
                .filter(mi -> mi.getId().equals(menuItemId))
                .findFirst();
    }

    public MenuItemDTO getMenuItem(long restaurantId, String menuItemId) {
        return findMenuItem(restaurantId, menuItemId)
                .orElseThrow(() -> new InvalidMenuItemIdException(menuItemId));
    }

    public Money getMenuItemPrice(long restaurantId, String menuItemId) {
        return getMenuItem(restaurantId, menuItemId).getPrice();
    }

    public String getMenuItemName(long restaurantId, String menuItemId) {
        return getMenuItem(restaurantId, menuItemId).getName();
    }
}
