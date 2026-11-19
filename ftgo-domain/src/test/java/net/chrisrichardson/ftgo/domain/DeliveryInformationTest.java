package net.chrisrichardson.ftgo.domain;

import org.junit.Test;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DeliveryInformationTest {

  @Test
  public void shouldMapDeliveryAddressToExistingColumns() throws NoSuchFieldException {
    AttributeOverrides overrides = DeliveryInformation.class.getDeclaredField("deliveryAddress")
            .getAnnotation(AttributeOverrides.class);
    assertNotNull(overrides);

    Map<String, String> columns = new HashMap<>();
    for (AttributeOverride override : overrides.value()) {
      columns.put(override.name(), override.column().name());
    }

    assertEquals(7, columns.size());
    assertEquals("delivery_address_street1", columns.get("street1"));
    assertEquals("delivery_address_street2", columns.get("street2"));
    assertEquals("delivery_address_city", columns.get("city"));
    assertEquals("delivery_address_state", columns.get("state"));
    assertEquals("delivery_address_zip", columns.get("zip"));
    assertEquals("delivery_address_latitude", columns.get("latitude"));
    assertEquals("delivery_address_longitude", columns.get("longitude"));
  }
}
