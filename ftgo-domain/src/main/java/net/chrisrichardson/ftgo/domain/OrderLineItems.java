package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Embeddable
public class OrderLineItems {

  @ElementCollection
  @CollectionTable(name = "order_line_items")
  private List<OrderLineItem> lineItems;

  private OrderLineItems() {
  }

  public OrderLineItems(List<OrderLineItem> lineItems) {
    this.lineItems = lineItems;
  }

  public List<OrderLineItem> getLineItems() {
    return lineItems;
  }

  public void setLineItems(List<OrderLineItem> lineItems) {
    this.lineItems = lineItems;
  }

  OrderLineItem findOrderLineItem(String lineItemId) {
    return lineItems.stream()
            .filter(li -> li.getMenuItemId().equals(lineItemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown menu item id: " + lineItemId));
  }

  Money changeToOrderTotal(OrderRevision orderRevision) {
    AtomicReference<Money> delta = new AtomicReference<>(Money.ZERO);

    revisedQuantities(orderRevision).forEach((lineItemId, newQuantity) -> {
      OrderLineItem lineItem = findOrderLineItem(lineItemId);
      delta.set(delta.get().add(lineItem.deltaForChangedQuantity(newQuantity)));
    });
    return delta.get();
  }

  void updateLineItems(OrderRevision orderRevision) {
    revisedQuantities(orderRevision).forEach((lineItemId, newQuantity) ->
            findOrderLineItem(lineItemId).setQuantity(newQuantity));
  }

  static Map<String, Integer> revisedQuantities(OrderRevision orderRevision) {
    Map<String, Integer> revisedQuantities = orderRevision.getRevisedLineItemQuantities();
    if (revisedQuantities == null) {
      return Collections.emptyMap();
    }
    revisedQuantities.forEach((lineItemId, newQuantity) -> {
      if (newQuantity == null) {
        throw new IllegalArgumentException("Missing quantity for menu item id: " + lineItemId);
      }
    });
    return revisedQuantities;
  }

  Money orderTotal() {
    return lineItems.stream().map(OrderLineItem::getTotal).reduce(Money.ZERO, Money::add);
  }

  LineItemQuantityChange lineItemQuantityChange(OrderRevision orderRevision) {
    Money currentOrderTotal = orderTotal();
    Money delta = changeToOrderTotal(orderRevision);
    Money newOrderTotal = currentOrderTotal.add(delta);
    return new LineItemQuantityChange(currentOrderTotal, newOrderTotal, delta);
  }
}