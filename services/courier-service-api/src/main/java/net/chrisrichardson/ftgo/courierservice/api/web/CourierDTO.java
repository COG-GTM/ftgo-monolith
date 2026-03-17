package net.chrisrichardson.ftgo.courierservice.api.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;

/**
 * DTO for cross-service communication representing a Courier.
 * Replaces direct entity sharing of {@code Courier} entity from ftgo-domain.
 *
 * <p>Service ownership: Courier Service</p>
 */
public class CourierDTO {

  private long id;
  private PersonName name;
  private Address address;
  private boolean available;

  private CourierDTO() {
  }

  public CourierDTO(long id, PersonName name, Address address, boolean available) {
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
