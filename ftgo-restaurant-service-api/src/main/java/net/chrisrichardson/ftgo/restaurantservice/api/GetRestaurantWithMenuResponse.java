package net.chrisrichardson.ftgo.restaurantservice.api;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;

public class GetRestaurantWithMenuResponse {
    private Long id;
    private String name;
    private Address address;
    private RestaurantMenuDTO menu;

    public GetRestaurantWithMenuResponse() {
    }

    public GetRestaurantWithMenuResponse(Long id, String name, Address address, RestaurantMenuDTO menu) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.menu = menu;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public RestaurantMenuDTO getMenu() {
        return menu;
    }

    public void setMenu(RestaurantMenuDTO menu) {
        this.menu = menu;
    }
}
