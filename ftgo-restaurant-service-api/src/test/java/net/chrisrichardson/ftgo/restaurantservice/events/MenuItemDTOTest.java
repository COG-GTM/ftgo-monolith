package net.chrisrichardson.ftgo.restaurantservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class MenuItemDTOTest {

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new MoneyModule());

  @Test
  public void constructorAndSettersStoreFields() {
    MenuItemDTO item = new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34"));
    assertEquals("1", item.getId());
    assertEquals("Chicken Vindaloo", item.getName());
    assertEquals(new Money("12.34"), item.getPrice());

    item.setId("2");
    item.setName("Lamb Korma");
    item.setPrice(new Money("15.00"));
    assertEquals("2", item.getId());
    assertEquals("Lamb Korma", item.getName());
    assertEquals(new Money("15.00"), item.getPrice());
  }

  @Test
  public void equalsAndHashCodeCompareAllFields() {
    MenuItemDTO item = new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34"));
    MenuItemDTO same = new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34"));

    assertEquals(item, same);
    assertEquals(item.hashCode(), same.hashCode());
    assertNotEquals(item, new MenuItemDTO("2", "Chicken Vindaloo", new Money("12.34")));
    assertNotEquals(item, new MenuItemDTO("1", "Lamb Korma", new Money("12.34")));
    assertNotEquals(item, new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.35")));
    assertNotEquals(item, null);
  }

  @Test
  public void toStringIncludesFieldValues() {
    String s = new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34")).toString();

    assertTrue(s, s.contains("id=1"));
    assertTrue(s, s.contains("name=Chicken Vindaloo"));
    assertTrue(s, s.contains("12.34"));
  }

  @Test
  public void roundTripsThroughJsonWithMoneyAsString() throws Exception {
    MenuItemDTO item = new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34"));

    String json = objectMapper.writeValueAsString(item);

    assertEquals("{\"id\":\"1\",\"name\":\"Chicken Vindaloo\",\"price\":\"12.34\"}", json);
    assertEquals(item, objectMapper.readValue(json, MenuItemDTO.class));
  }
}
