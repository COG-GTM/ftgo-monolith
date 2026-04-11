package net.chrisrichardson.ftgo.restaurantservice.events;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Response after creating a restaurant")
public class CreateRestaurantResponse {
  @ApiModelProperty(value = "The created restaurant ID")
  private long id;

  private CreateRestaurantResponse() {
  }

  public CreateRestaurantResponse(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }
}
