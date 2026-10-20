package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import static org.junit.Assert.*;

public class MenuItemTest {

  @Test
  public void reflectionEqualityAndHashCodeUseIdNameAndPrice() {
    MenuItem first = item("soup", "Soup", "4.50");
    MenuItem same = item("soup", "Soup", "4.50");

    assertEquals(first, same);
    assertEquals(first.hashCode(), same.hashCode());
    assertNotEquals(first, item("salad", "Soup", "4.50"));
    assertNotEquals(first, item("soup", "Salad", "4.50"));
    assertNotEquals(first, item("soup", "Soup", "5.00"));
  }

  @Test
  public void toStringIncludesIdNameAndPrice() {
    String description = item("soup", "Tomato Soup", "4.50").toString();

    assertTrue(description.contains("soup"));
    assertTrue(description.contains("Tomato Soup"));
    assertTrue(description.contains("4.50"));
  }

  @Test
  public void settersChangeEquality() {
    MenuItem item = item("soup", "Soup", "4.50");
    MenuItem original = item("soup", "Soup", "4.50");

    item.setId("salad");
    assertNotEquals(original, item);
    item.setId("soup");
    item.setName("Salad");
    assertNotEquals(original, item);
    item.setName("Soup");
    item.setPrice(new Money("5.00"));
    assertNotEquals(original, item);
  }

  private static MenuItem item(String id, String name, String price) {
    return new MenuItem(id, name, new Money(price));
  }

  @Test
  public void gettersReturnConstructorValues() {
    MenuItem menuItem = item("soup", "Soup", "4.50");

    assertEquals("soup", menuItem.getId());
    assertEquals("Soup", menuItem.getName());
    assertEquals(new Money("4.50"), menuItem.getPrice());
  }
}
