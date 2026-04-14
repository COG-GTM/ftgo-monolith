package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Money;
import org.junit.Test;

import static org.junit.Assert.*;

public class MenuItemTest {

  @Test
  public void shouldCreateMenuItem() {
    MenuItem item = new MenuItem("1", "Burger", new Money("10.00"));
    assertEquals("1", item.getId());
    assertEquals("Burger", item.getName());
    assertEquals(new Money("10.00"), item.getPrice());
  }

  @Test
  public void shouldSetProperties() {
    MenuItem item = new MenuItem("1", "Burger", new Money("10.00"));
    item.setId("2");
    item.setName("Fries");
    item.setPrice(new Money("5.00"));
    assertEquals("2", item.getId());
    assertEquals("Fries", item.getName());
    assertEquals(new Money("5.00"), item.getPrice());
  }

  @Test
  public void shouldHaveEqualityBasedOnFields() {
    MenuItem item1 = new MenuItem("1", "Burger", new Money("10.00"));
    MenuItem item2 = new MenuItem("1", "Burger", new Money("10.00"));
    assertEquals(item1, item2);
    assertEquals(item1.hashCode(), item2.hashCode());
  }

  @Test
  public void shouldNotBeEqualForDifferentFields() {
    MenuItem item1 = new MenuItem("1", "Burger", new Money("10.00"));
    MenuItem item2 = new MenuItem("2", "Fries", new Money("5.00"));
    assertNotEquals(item1, item2);
  }

  @Test
  public void shouldHaveToString() {
    MenuItem item = new MenuItem("1", "Burger", new Money("10.00"));
    assertNotNull(item.toString());
  }
}
