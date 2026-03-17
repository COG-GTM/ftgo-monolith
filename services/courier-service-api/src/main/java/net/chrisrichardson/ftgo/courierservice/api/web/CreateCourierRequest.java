package net.chrisrichardson.ftgo.courierservice.api.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;

/**
 * Request DTO for creating a new courier.
 *
 * <p>Service ownership: Courier Service</p>
 */
public class CreateCourierRequest {

  private PersonName name;
  private Address address;

  private CreateCourierRequest() {
  }

  public CreateCourierRequest(PersonName name, Address address) {
    this.name = name;
    this.address = address;
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
}
