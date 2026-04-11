package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.util.List;

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
            .orElseThrow(() -> new IllegalArgumentException("Line item not found: " + lineItemId));
  }

  // Using Java 8 stream reduce() instead of AtomicReference accumulation pattern
  Money changeToOrderTotal(OrderRevision orderRevision) {
    return orderRevision.getRevisedLineItemQuantities().entrySet().stream()
            .map(entry -> findOrderLineItem(entry.getKey()).deltaForChangedQuantity(entry.getValue()))
            .reduce(Money.ZERO, Money::add);
  }

  void updateLineItems(OrderRevision orderRevision) {
    // Using Iterable.forEach() directly instead of stream().forEach()
    getLineItems().forEach(li -> {
      Integer revised = orderRevision.getRevisedLineItemQuantities().get(li.getMenuItemId());
      li.setQuantity(revised);
    });
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
