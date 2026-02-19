package net.chrisrichardson.ftgo.restaurantservice.events;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Restaurant details response")
public class GetRestaurantResponse {
  @ApiModelProperty(value = "Restaurant ID")
  private long id;
  @ApiModelProperty(value = "Restaurant name")
  private String name;

  private GetRestaurantResponse() {
  }

  public GetRestaurantResponse(long id, String name) {
    this.id = id;
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
