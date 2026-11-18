package net.chrisrichardson.ftgo.domain;

import org.junit.Test;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DeliveryInformationTest {

  @Test
  public void shouldMapDeliveryAddressCoordinatesToExistingColumns() throws NoSuchFieldException {
    AttributeOverrides overrides = DeliveryInformation.class.getDeclaredField("deliveryAddress")
            .getAnnotation(AttributeOverrides.class);
    Map<String, String> columns = new HashMap<>();
    for (AttributeOverride override : overrides.value()) {
      columns.put(override.name(), override.column().name());
    }

    assertEquals("delivery_address_latitude", columns.get("latitude"));
    assertEquals("delivery_address_longitude", columns.get("longitude"));
  }
}
