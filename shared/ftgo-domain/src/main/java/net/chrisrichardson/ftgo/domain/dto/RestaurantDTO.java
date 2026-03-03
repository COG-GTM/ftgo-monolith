package net.chrisrichardson.ftgo.domain.dto;

import java.util.List;

/**
 * Data Transfer Object for Restaurant entity used in cross-service communication.
 * Decouples the JPA entity from the API contract.
 */
public class RestaurantDTO {

    private Long restaurantId;
    private String name;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zip;
    private List<MenuItemDTO> menuItems;

    public RestaurantDTO() {
    }

    public RestaurantDTO(Long restaurantId, String name, String street1, String street2,
                         String city, String state, String zip, List<MenuItemDTO> menuItems) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.street1 = street1;
        this.street2 = street2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.menuItems = menuItems;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet1() {
        return street1;
    }

    public void setStreet1(String street1) {
        this.street1 = street1;
    }

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public List<MenuItemDTO> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItemDTO> menuItems) {
        this.menuItems = menuItems;
    }
}
