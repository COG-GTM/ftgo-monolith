package net.chrisrichardson.ftgo.consumerservice.api.web;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.chrisrichardson.ftgo.common.PersonName;

@ApiModel(description = "Request to create a new consumer")
public class CreateConsumerRequest {
  @ApiModelProperty(value = "Consumer name", required = true)
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
