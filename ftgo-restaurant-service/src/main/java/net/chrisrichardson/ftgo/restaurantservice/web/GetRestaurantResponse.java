package net.chrisrichardson.ftgo.restaurantservice.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;

public class GetRestaurantResponse {
  private Long id;
  private String name;
  private RestaurantMenuDTO menu;

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

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public RestaurantMenuDTO getMenu() {
    return menu;
  }

  public GetRestaurantResponse() {
  }

  public GetRestaurantResponse(Long id, String name) {
    this(id, name, null);
  }

  public GetRestaurantResponse(Long id, String name, RestaurantMenuDTO menu) {
    this.id = id;
    this.name = name;
    this.menu = menu;
  }
}
