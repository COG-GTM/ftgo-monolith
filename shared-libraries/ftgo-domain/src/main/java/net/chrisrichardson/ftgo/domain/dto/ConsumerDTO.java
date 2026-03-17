package net.chrisrichardson.ftgo.domain.dto;

/**
 * Cross-service DTO representing a Consumer.
 * Used by Order Service for consumer validation and display.
 */
public class ConsumerDTO {

  private long id;
  private String firstName;
  private String lastName;

  public ConsumerDTO() {
  }

  public ConsumerDTO(long id, String firstName, String lastName) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
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
}
