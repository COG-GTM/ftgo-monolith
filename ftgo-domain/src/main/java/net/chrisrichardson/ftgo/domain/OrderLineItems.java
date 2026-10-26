package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Embeddable
public class OrderLineItems {

  static final int MAX_LINE_ITEM_QUANTITY = 100;

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
            .orElseThrow(() -> new InvalidOrderRevisionException("Order does not contain menu item id " + lineItemId));
  }

  void validateRevision(OrderRevision orderRevision) {
    orderRevision.getRevisedLineItemQuantities().forEach((lineItemId, newQuantity) -> {
      if (newQuantity == null)
        throw new InvalidOrderRevisionException("Missing quantity for menu item id " + lineItemId);
      if (newQuantity < 1 || newQuantity > MAX_LINE_ITEM_QUANTITY)
        throw new InvalidOrderRevisionException("Quantity for menu item id " + lineItemId
                + " must be between 1 and " + MAX_LINE_ITEM_QUANTITY);
      findOrderLineItem(lineItemId);
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
    Map<String, Integer> revisedQuantities = orderRevision.getRevisedLineItemQuantities();
    getLineItems().forEach(li ->
            Optional.ofNullable(revisedQuantities.get(li.getMenuItemId())).ifPresent(li::setQuantity));
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