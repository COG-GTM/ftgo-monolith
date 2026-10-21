package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MenuItemPriceTest {

  @Test
  public void shouldAcceptPositivePrice() {
    MenuItem item = new MenuItem("1", "Chicken Vindaloo", new Money("12.34"));
    assertEquals(new Money("12.34"), item.getPrice());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectNegativePrice() {
    new MenuItem("1", "Refund", new Money("-500.00"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectZeroPrice() {
    new MenuItem("1", "Free", Money.ZERO);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectNullPrice() {
    new MenuItem("1", "Missing", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectBlankId() {
    new MenuItem(" ", "Chicken Vindaloo", new Money("12.34"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectNegativePriceOnUpdate() {
    new MenuItem("1", "Chicken Vindaloo", new Money("12.34")).setPrice(new Money("-1"));
  }
}
