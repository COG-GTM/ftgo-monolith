package net.chrisrichardson.ftgo.orderservice.web;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MenuItemIdAndQuantityTest {

  @Test
  public void constructorStoresMenuItemIdAndQuantity() {
    MenuItemIdAndQuantity item = new MenuItemIdAndQuantity("1", 3);

    assertEquals("1", item.getMenuItemId());
    assertEquals(3, item.getQuantity());
  }

  @Test
  public void setMenuItemIdReplacesIdAndKeepsQuantity() {
    MenuItemIdAndQuantity item = new MenuItemIdAndQuantity("1", 3);

    item.setMenuItemId("2");

    assertEquals("2", item.getMenuItemId());
    assertEquals(3, item.getQuantity());
  }
}
