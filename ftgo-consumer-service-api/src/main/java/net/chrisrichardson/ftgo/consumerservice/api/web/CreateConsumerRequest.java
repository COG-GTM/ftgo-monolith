package net.chrisrichardson.ftgo.consumerservice.api.web;

import net.chrisrichardson.ftgo.common.PersonName;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class CreateConsumerRequest {
  @NotNull
  @Valid
  private PersonName name;

  public PersonName getName() {
    return name;
  }

  public void setName(PersonName name) {
    this.name = name;
  }

  public CreateConsumerRequest(PersonName name) {

    this.name = name;
  }

  private CreateConsumerRequest() {
  }


}
