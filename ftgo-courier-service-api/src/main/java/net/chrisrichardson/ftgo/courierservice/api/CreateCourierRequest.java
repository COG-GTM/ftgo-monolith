package net.chrisrichardson.ftgo.courierservice.api;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class CreateCourierRequest {
  @NotNull
  @Valid
  private PersonName name;
  @NotNull
  @Valid
  private Address address;

  public CreateCourierRequest() {
  }

  public CreateCourierRequest(PersonName name, Address address) {
    this.name = name;
    this.address = address;
  }

  public PersonName getName() {
    return name;
  }

  public Address getAddress() {
    return address;
  }

  public void setName(PersonName name) {
    this.name = name;
  }

  public void setAddress(Address address) {
    this.address = address;
  }
}
