package net.chrisrichardson.ftgo.consumerservice.api.web;

import net.chrisrichardson.ftgo.common.PersonName;

/**
 * DTO for cross-service communication representing a Consumer.
 * Replaces direct entity sharing of {@code Consumer} entity from ftgo-domain.
 *
 * <p>Service ownership: Consumer Service</p>
 */
public class ConsumerDTO {

  private long id;
  private PersonName name;

  private ConsumerDTO() {
  }

  public ConsumerDTO(long id, PersonName name) {
    this.id = id;
    this.name = name;
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
}
