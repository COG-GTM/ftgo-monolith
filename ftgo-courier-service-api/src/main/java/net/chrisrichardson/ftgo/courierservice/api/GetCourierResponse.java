package net.chrisrichardson.ftgo.courierservice.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;

@ApiModel(description = "Courier details response")
public class GetCourierResponse {
  @ApiModelProperty(value = "Courier ID")
  private long id;
  @ApiModelProperty(value = "Courier name")
  private PersonName name;
  @ApiModelProperty(value = "Courier address")
  private Address address;
  @ApiModelProperty(value = "Whether the courier is currently available")
  private boolean available;

  private GetCourierResponse() {
  }

  public GetCourierResponse(long id, PersonName name, Address address, boolean available) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.available = available;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public PersonName getName() {
    return name;
  }

  public void setName(PersonName name) {
    this.name = name;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }
}
