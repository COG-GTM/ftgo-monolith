package net.chrisrichardson.ftgo.restaurantservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class CreateRestaurantRequestTest {

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new MoneyModule());

  private static RestaurantMenuDTO menu() {
    return new RestaurantMenuDTO(Collections.singletonList(
            new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34"))));
  }

  @Test
  public void constructorAndSettersStoreFields() {
    Address address = new Address("1 Main St", null, "Oakland", "CA", "94612");
    RestaurantMenuDTO menu = menu();
    CreateRestaurantRequest request = new CreateRestaurantRequest("Ajanta", address, menu);
    assertEquals("Ajanta", request.getName());
    assertSame(address, request.getAddress());
    assertSame(menu, request.getMenu());

    Address otherAddress = new Address("2 Main St", null, "Berkeley", "CA", "94704");
    RestaurantMenuDTO otherMenu = new RestaurantMenuDTO(Collections.emptyList());
    request.setName("Mumbai Grill");
    request.setAddress(otherAddress);
    request.setMenu(otherMenu);
    assertEquals("Mumbai Grill", request.getName());
    assertSame(otherAddress, request.getAddress());
    assertSame(otherMenu, request.getMenu());
  }

  @Test
  public void deserializesFromJsonUsingPrivateNoArgConstructor() throws Exception {
    String json = "{\"name\":\"Ajanta\","
            + "\"address\":{\"street1\":\"1 Main St\",\"city\":\"Oakland\",\"state\":\"CA\",\"zip\":\"94612\"},"
            + "\"menu\":{\"menuItemDTOs\":[{\"id\":\"1\",\"name\":\"Chicken Vindaloo\",\"price\":\"12.34\"}]}}";

    CreateRestaurantRequest request = objectMapper.readValue(json, CreateRestaurantRequest.class);

    assertEquals("Ajanta", request.getName());
    assertEquals("1 Main St", request.getAddress().getStreet1());
    assertEquals("Oakland", request.getAddress().getCity());
    assertEquals("94612", request.getAddress().getZip());
    assertEquals(menu(), request.getMenu());
  }

  @Test
  public void serializesMenuItemsWithPricesAsStrings() throws Exception {
    CreateRestaurantRequest request = new CreateRestaurantRequest("Ajanta",
            new Address("1 Main St", null, "Oakland", "CA", "94612"), menu());

    CreateRestaurantRequest roundTripped = objectMapper.readValue(
            objectMapper.writeValueAsString(request), CreateRestaurantRequest.class);

    assertEquals("Ajanta", roundTripped.getName());
    assertEquals("Oakland", roundTripped.getAddress().getCity());
    assertEquals(new Money("12.34"), roundTripped.getMenu().getMenuItemDTOs().get(0).getPrice());
    assertEquals(request.getMenu(), roundTripped.getMenu());
  }
}
