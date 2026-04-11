package net.chrisrichardson.ftgo.consumerservice.api.web;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.chrisrichardson.ftgo.common.PersonName;

@ApiModel(description = "Consumer details response")
public class GetConsumerResponse {
  @ApiModelProperty(value = "Consumer ID")
  private long consumerId;
  @ApiModelProperty(value = "Consumer name")
  private PersonName name;

  private GetConsumerResponse() {
  }

  public GetConsumerResponse(long consumerId, PersonName name) {
    this.consumerId = consumerId;
    this.name = name;
  }

  public GetConsumerResponse(PersonName name) {
    this.name = name;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(long consumerId) {
    this.consumerId = consumerId;
  }

  public PersonName getName() {
    return name;
  }

  public void setName(PersonName name) {
    this.name = name;
  }
}
