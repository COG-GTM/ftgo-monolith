package net.chrisrichardson.ftgo.restaurantservice.api;

public class RestaurantNotFoundException extends RuntimeException {
    private final long restaurantId;

    public RestaurantNotFoundException(long restaurantId) {
        super("Restaurant not found with id " + restaurantId);
        this.restaurantId = restaurantId;
    }

    public long getRestaurantId() {
        return restaurantId;
    }
}
