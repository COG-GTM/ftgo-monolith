package net.chrisrichardson.ftgo.orderservice.web;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MenuItemIdAndQuantityTest {

  @Test
  public void shouldCreateWithConstructor() {
    MenuItemIdAndQuantity item = new MenuItemIdAndQuantity("m1", 3);
    assertThat(item.getMenuItemId()).isEqualTo("m1");
    assertThat(item.getQuantity()).isEqualTo(3);
  }

  @Test
  public void shouldSetMenuItemId() {
    MenuItemIdAndQuantity item = new MenuItemIdAndQuantity("m1", 1);
    item.setMenuItemId("m2");
    assertThat(item.getMenuItemId()).isEqualTo("m2");
  }
}
