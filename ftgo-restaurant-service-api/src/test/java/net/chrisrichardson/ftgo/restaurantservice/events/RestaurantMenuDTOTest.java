package net.chrisrichardson.ftgo.restaurantservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RestaurantMenuDTOTest {

  private static final MenuItemDTO VINDALOO = new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34"));
  private static final MenuItemDTO KORMA = new MenuItemDTO("2", "Lamb Korma", new Money("15.00"));

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new MoneyModule());

  @Test
  public void constructorAndSetterStoreMenuItems() {
    List<MenuItemDTO> items = Collections.singletonList(VINDALOO);
    RestaurantMenuDTO menu = new RestaurantMenuDTO(items);
    assertSame(items, menu.getMenuItemDTOs());

    List<MenuItemDTO> replacement = Arrays.asList(VINDALOO, KORMA);
    menu.setMenuItemDTOs(replacement);
    assertSame(replacement, menu.getMenuItemDTOs());
  }

  @Test
  public void equalsAndHashCodeCompareMenuItems() {
    RestaurantMenuDTO menu = new RestaurantMenuDTO(Arrays.asList(VINDALOO, KORMA));
    RestaurantMenuDTO same = new RestaurantMenuDTO(Arrays.asList(
            new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34")),
            new MenuItemDTO("2", "Lamb Korma", new Money("15.00"))));

    assertEquals(menu, same);
    assertEquals(menu.hashCode(), same.hashCode());
    assertNotEquals(menu, new RestaurantMenuDTO(Collections.singletonList(VINDALOO)));
    assertNotEquals(menu, null);
  }

  @Test
  public void toStringIncludesMenuItems() {
    String s = new RestaurantMenuDTO(Collections.singletonList(VINDALOO)).toString();

    assertTrue(s, s.contains("menuItems="));
    assertTrue(s, s.contains("Chicken Vindaloo"));
  }

  @Test
  public void jsonUsesMenuItemDTOsPropertyAndRoundTrips() throws Exception {
    RestaurantMenuDTO menu = new RestaurantMenuDTO(Collections.singletonList(VINDALOO));

    String json = objectMapper.writeValueAsString(menu);

    assertEquals("{\"menuItemDTOs\":[{\"id\":\"1\",\"name\":\"Chicken Vindaloo\",\"price\":\"12.34\"}]}", json);
    assertEquals(menu, objectMapper.readValue(json, RestaurantMenuDTO.class));
  }
}
