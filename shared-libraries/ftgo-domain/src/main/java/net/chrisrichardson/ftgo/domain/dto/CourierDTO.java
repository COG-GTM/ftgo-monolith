package net.chrisrichardson.ftgo.domain.dto;

/**
 * Cross-service DTO representing a Courier.
 * Used by Order Service for delivery scheduling.
 */
public class CourierDTO {

  private long id;
  private String firstName;
  private String lastName;
  private boolean available;

  public CourierDTO() {
  }

  public CourierDTO(long id, String firstName, String lastName, boolean available) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.available = available;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }
}
