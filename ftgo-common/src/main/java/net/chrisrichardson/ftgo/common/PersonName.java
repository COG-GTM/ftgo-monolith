package net.chrisrichardson.ftgo.common;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;

@Embeddable
public class PersonName {
  @NotBlank
  private String firstName;
  @NotBlank
  private String lastName;

  private PersonName() {
  }

  public PersonName(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
}