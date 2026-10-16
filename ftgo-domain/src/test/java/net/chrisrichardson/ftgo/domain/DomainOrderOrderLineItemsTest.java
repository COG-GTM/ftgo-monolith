package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import java.util.Collections;
import java.util.NoSuchElementException;

import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.CHICKEN_VINDALOO_ID;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.SAMOSA_ID;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.chickenVindaloo;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.lineItems;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.money;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.quantities;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.revision;
import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.samosa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DomainOrderOrderLineItemsTest {

  private OrderLineItems standardItems() {
    return new OrderLineItems(lineItems(chickenVindaloo(5), samosa(2)));
  }

  @Test
  public void orderTotalSumsLineItemTotals() {
    assertThat(standardItems().orderTotal()).isEqualTo(money("68.70"));
  }

  @Test
  public void orderTotalOfNoLineItemsIsZero() {
    assertThat(new OrderLineItems(Collections.emptyList()).orderTotal()).isEqualTo(Money.ZERO);
  }

  @Test
  public void findOrderLineItemReturnsItemByMenuItemId() {
    assertThat(standardItems().findOrderLineItem(SAMOSA_ID).getName()).isEqualTo("Samosa");
  }

  @Test
  public void findOrderLineItemThrowsForUnknownMenuItemId() {
    assertThatThrownBy(() -> standardItems().findOrderLineItem("does-not-exist"))
            .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  public void changeToOrderTotalSumsDeltasOfAllRevisedItems() {
    Money delta = standardItems().changeToOrderTotal(
            revision(quantities(CHICKEN_VINDALOO_ID, 6, SAMOSA_ID, 0)));
    // +12.34 for one more vindaloo, -7.00 for removing both samosas
    assertThat(delta).isEqualTo(money("5.34"));
  }

  @Test
  public void changeToOrderTotalIsZeroForEmptyRevision() {
    assertThat(standardItems().changeToOrderTotal(revision(Collections.emptyMap()))).isEqualTo(Money.ZERO);
  }

  @Test
  public void lineItemQuantityChangeReportsCurrentNewAndDelta() {
    LineItemQuantityChange change = standardItems().lineItemQuantityChange(
            revision(quantities(CHICKEN_VINDALOO_ID, 3)));

    assertThat(change.getCurrentOrderTotal()).isEqualTo(money("68.70"));
    assertThat(change.getDelta()).isEqualTo(money("-24.68"));
    assertThat(change.getNewOrderTotal()).isEqualTo(money("44.02"));
  }

  @Test
  public void lineItemQuantityChangeDoesNotMutateLineItems() {
    OrderLineItems items = standardItems();
    items.lineItemQuantityChange(revision(quantities(CHICKEN_VINDALOO_ID, 3)));
    assertThat(items.orderTotal()).isEqualTo(money("68.70"));
  }

  @Test
  public void updateLineItemsAppliesRevisedQuantities() {
    OrderLineItems items = standardItems();
    items.updateLineItems(revision(quantities(CHICKEN_VINDALOO_ID, 1, SAMOSA_ID, 4)));

    assertThat(items.getLineItems()).extracting(OrderLineItem::getQuantity).containsExactly(1, 4);
    assertThat(items.orderTotal()).isEqualTo(money("26.34"));
  }

  // Suspected bug: updateLineItems unboxes the revised quantity for EVERY line item, so a revision
  // that omits any line item fails with a NullPointerException instead of leaving that item unchanged.
  @Test
  public void updateLineItemsWithPartialRevisionCurrentlyThrowsNullPointerException() {
    OrderLineItems items = standardItems();
    assertThatThrownBy(() -> items.updateLineItems(revision(quantities(SAMOSA_ID, 4))))
            .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void setLineItemsReplacesItemsUsedForTotal() {
    OrderLineItems items = standardItems();
    items.setLineItems(lineItems(samosa(1)));
    assertThat(items.orderTotal()).isEqualTo(money("3.50"));
  }
}
