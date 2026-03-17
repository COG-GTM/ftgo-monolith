package net.chrisrichardson.ftgo.orderservice.api.web;

import java.util.Map;

/**
 * Request DTO for revising an existing order.
 *
 * <p>Service ownership: Order Service</p>
 */
public class ReviseOrderRequest {

  private Map<String, Integer> revisedLineItemQuantities;

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
