package net.chrisrichardson.ftgo.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OrderRevision {

  private Optional<DeliveryInformation> deliveryInformation = Optional.empty();
  private Map<String, Integer> revisedLineItemQuantities = Collections.emptyMap();

  private OrderRevision() {
  }

  public OrderRevision(Optional<DeliveryInformation> deliveryInformation, Map<String, Integer> revisedLineItemQuantities) {
    this.deliveryInformation = deliveryInformation;
    setRevisedLineItemQuantities(revisedLineItemQuantities);
  }

  public void setDeliveryInformation(Optional<DeliveryInformation> deliveryInformation) {
    this.deliveryInformation = deliveryInformation;
  }

  public void setRevisedLineItemQuantities(Map<String, Integer> revisedLineItemQuantities) {
    this.revisedLineItemQuantities = revisedLineItemQuantities == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new HashMap<>(revisedLineItemQuantities));
  }

  public Optional<DeliveryInformation> getDeliveryInformation() {
    return deliveryInformation;
  }


  public Map<String, Integer> getRevisedLineItemQuantities() {
    return revisedLineItemQuantities;
  }
}
