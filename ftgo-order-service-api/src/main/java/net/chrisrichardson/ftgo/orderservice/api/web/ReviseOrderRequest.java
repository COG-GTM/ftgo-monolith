package net.chrisrichardson.ftgo.orderservice.api.web;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

public class ReviseOrderRequest {

  public static final int MAX_LINE_ITEM_QUANTITY = 100;

  @NotEmpty
  private Map<String, @NotNull @Min(1) @Max(MAX_LINE_ITEM_QUANTITY) Integer> revisedLineItemQuantities;

  private ReviseOrderRequest() {
  }

  public ReviseOrderRequest(Map<String, Integer> revisedLineItemQuantities) {
    this.revisedLineItemQuantities = revisedLineItemQuantities;
  }

  public Map<String, Integer> getRevisedLineItemQuantities() {
    return revisedLineItemQuantities;
  }

  public void setRevisedLineItemQuantities(Map<String, Integer> revisedLineItemQuantities) {
    this.revisedLineItemQuantities = revisedLineItemQuantities;
  }
}
