package net.chrisrichardson.ftgo.common;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class PersonName {
  private String firstName;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PersonName that = (PersonName) o;
    return Objects.equals(firstName, that.firstName) &&
            Objects.equals(lastName, that.lastName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstName, lastName);
  }

  @Override
  public String toString() {
    return "PersonName{firstName='" + firstName + "', lastName='" + lastName + "'}";
  }
}
