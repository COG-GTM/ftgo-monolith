package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.util.List;
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
    return lineItems.stream().filter(li -> li.getMenuItemId().equals(lineItemId)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown line item " + lineItemId));
  }

  void validateRevision(OrderRevision orderRevision) {
    if (orderRevision.getRevisedLineItemQuantities() == null) {
      throw new IllegalArgumentException("Revision must specify line item quantities");
    }
    orderRevision.getRevisedLineItemQuantities().forEach((lineItemId, newQuantity) -> {
      findOrderLineItem(lineItemId);
      if (newQuantity == null) {
        throw new IllegalArgumentException("Missing quantity for line item " + lineItemId);
      }
      OrderLineItem.requirePositiveQuantity(newQuantity);
    });
  }

  Money changeToOrderTotal(OrderRevision orderRevision) {
    AtomicReference<Money> delta = new AtomicReference<>(Money.ZERO);

    orderRevision.getRevisedLineItemQuantities().forEach((lineItemId, newQuantity) -> {
      OrderLineItem lineItem = findOrderLineItem(lineItemId);
      delta.set(delta.get().add(lineItem.deltaForChangedQuantity(newQuantity)));
    });
    return delta.get();
  }

  void updateLineItems(OrderRevision orderRevision) {
    getLineItems().stream().forEach(li -> {
      Integer revised = orderRevision.getRevisedLineItemQuantities().get(li.getMenuItemId());
      if (revised != null) {
        li.setQuantity(revised);
      }
    });
  }

  Money orderTotal() {
    return lineItems.stream().map(OrderLineItem::getTotal).reduce(Money.ZERO, Money::add);
  }

  LineItemQuantityChange lineItemQuantityChange(OrderRevision orderRevision) {
    validateRevision(orderRevision);
    Money currentOrderTotal = orderTotal();
    Money delta = changeToOrderTotal(orderRevision);
    Money newOrderTotal = currentOrderTotal.add(delta);
    return new LineItemQuantityChange(currentOrderTotal, newOrderTotal, delta);
  }
}