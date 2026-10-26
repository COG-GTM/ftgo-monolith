package net.chrisrichardson.ftgo.orderservice.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MenuItemIdAndQuantityTest {

  @Test
  void shouldCreateMenuItemIdAndQuantity() {
    MenuItemIdAndQuantity item = new MenuItemIdAndQuantity("item1", 3);

    assertThat(item.getMenuItemId()).isEqualTo("item1");
    assertThat(item.getQuantity()).isEqualTo(3);
  }

  @Test
  void shouldSupportSetters() {
    MenuItemIdAndQuantity item = new MenuItemIdAndQuantity("item1", 1);
    item.setMenuItemId("item2");

    assertThat(item.getMenuItemId()).isEqualTo("item2");
  }
}
