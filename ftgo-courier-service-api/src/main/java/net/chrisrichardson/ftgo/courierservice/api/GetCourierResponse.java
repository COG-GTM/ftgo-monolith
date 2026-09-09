package net.chrisrichardson.ftgo.courierservice.api;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;

public class GetCourierResponse {

  private long courierId;
  private PersonName name;
  private Address address;
  private boolean available;

  public GetCourierResponse() {
  }

  public GetCourierResponse(long courierId, PersonName name, Address address, boolean available) {
    this.courierId = courierId;
    this.name = name;
    this.address = address;
    this.available = available;
  }

  public long getCourierId() {
    return courierId;
  }

  public void setCourierId(long courierId) {
    this.courierId = courierId;
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
