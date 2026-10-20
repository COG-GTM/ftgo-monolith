package net.chrisrichardson.ftgo.orderservice.api.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public class OrderLineItemDTOTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void noArgConstructorLeavesDefaults() {
    OrderLineItemDTO dto = new OrderLineItemDTO();
    assertEquals(0, dto.getQuantity());
    assertNull(dto.getMenuItemId());
    assertNull(dto.getName());
  }

  @Test
  public void constructorAssignsArgumentsInOrder() {
    OrderLineItemDTO dto = new OrderLineItemDTO(3, "1", "Chicken Vindaloo");
    assertEquals(3, dto.getQuantity());
    assertEquals("1", dto.getMenuItemId());
    assertEquals("Chicken Vindaloo", dto.getName());
  }

  @Test
  public void settersReplaceValues() {
    OrderLineItemDTO dto = new OrderLineItemDTO(1, "a", "A");
    dto.setQuantity(7);
    dto.setMenuItemId("b");
    dto.setName("B");

    assertEquals(7, dto.getQuantity());
    assertEquals("b", dto.getMenuItemId());
    assertEquals("B", dto.getName());
  }

  @Test
  public void roundTripsThroughJson() throws Exception {
    String json = objectMapper.writeValueAsString(new OrderLineItemDTO(2, "m-9", "Samosa"));
    JsonNode node = objectMapper.readTree(json);
    assertEquals(2, node.get("quantity").asInt());
    assertEquals("m-9", node.get("menuItemId").asText());
    assertEquals("Samosa", node.get("name").asText());

    OrderLineItemDTO copy = objectMapper.readValue(json, OrderLineItemDTO.class);
    assertEquals(2, copy.getQuantity());
    assertEquals("m-9", copy.getMenuItemId());
    assertEquals("Samosa", copy.getName());
  }

  @Test
  public void equals_usesIdentityNotValues_noEqualsOverride_bug() {
    // OrderLineItemDTO does not override equals/hashCode, so value-identical DTOs are unequal.
    assertNotEquals(new OrderLineItemDTO(1, "m", "n"), new OrderLineItemDTO(1, "m", "n"));
  }
}
