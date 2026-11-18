package net.chrisrichardson.ftgo.consumerservice.api.web;

import net.chrisrichardson.ftgo.common.PersonName;

public class UpdateConsumerRequest {
  private Long id;
  private PersonName name;

  public UpdateConsumerRequest() {
  }

  public UpdateConsumerRequest(Long id, PersonName name) {
    this.id = id;
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public PersonName getName() {
    return name;
  }

  public void setName(PersonName name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "UpdateConsumerRequest{id=" + id
            + ", firstName=" + (name == null ? null : name.getFirstName())
            + ", lastName=" + (name == null ? null : name.getLastName()) + "}";
  }
}
