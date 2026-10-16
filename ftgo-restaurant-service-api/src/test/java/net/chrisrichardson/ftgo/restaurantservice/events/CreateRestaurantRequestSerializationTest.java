package net.chrisrichardson.ftgo.restaurantservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// JUnit 5 (Jupiter) test: the restaurant API DTOs must round-trip through Jackson (as configured by
// ftgo-common's MoneyModule) on the Spring Boot 3.5 / Jackson stack. Guards the private no-arg
// constructors and the "menuItemDTOs" property name that REST clients depend on.
public class CreateRestaurantRequestSerializationTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  // Register the Money <-> JSON string (de)serializer once for all tests in this class.
  @BeforeAll
  public static void initialize() {
    objectMapper.registerModule(new MoneyModule());
  }

  // Builds a fully populated request: name, address and a one-item menu.
  private static CreateRestaurantRequest sampleRequest() {
    MenuItemDTO item = new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34"));
    RestaurantMenuDTO menu = new RestaurantMenuDTO(List.of(item));
    Address address = new Address("1 Main Street", "Unit 99", "Oakland", "CA", "94611");
    return new CreateRestaurantRequest("Ajanta", address, menu);
  }

  @Test
  public void shouldSerializeMenuItemsUnderExpectedPropertyNames() throws IOException {
    String json = objectMapper.writeValueAsString(sampleRequest());

    // Getter is getMenuItemDTOs(), so Jackson must expose the list as "menuItemDTOs".
    assertTrue(json.contains("\"menuItemDTOs\":[{"), json);
    // Money is written as a plain string by MoneyModule, not as a nested {"amount": ...} object.
    assertTrue(json.contains("\"price\":\"12.34\""), json);
    assertTrue(json.contains("\"name\":\"Ajanta\""), json);
    assertTrue(json.contains("\"city\":\"Oakland\""), json);
  }

  @Test
  public void shouldRoundTripThroughJson() throws IOException {
    CreateRestaurantRequest original = sampleRequest();

    String json = objectMapper.writeValueAsString(original);
    CreateRestaurantRequest restored = objectMapper.readValue(json, CreateRestaurantRequest.class);

    // CreateRestaurantRequest/Address have no equals(), so compare field by field; the menu DTOs do.
    assertEquals(original.getName(), restored.getName());
    assertEquals(original.getMenu(), restored.getMenu());
    assertEquals(original.getAddress().getStreet1(), restored.getAddress().getStreet1());
    assertEquals(original.getAddress().getZip(), restored.getAddress().getZip());
  }

  @Test
  public void shouldDeserializeMenuItemDtoWithMoneyString() throws IOException {
    MenuItemDTO item = objectMapper.readValue(
            "{\"id\":\"1\",\"name\":\"Chicken Vindaloo\",\"price\":\"12.34\"}", MenuItemDTO.class);

    assertEquals(new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34")), item);
  }
}
