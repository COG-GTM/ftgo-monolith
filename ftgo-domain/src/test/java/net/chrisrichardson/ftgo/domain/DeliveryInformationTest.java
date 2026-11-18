package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import org.junit.Test;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DeliveryInformationTest {

  @Test
  public void shouldMapLatitudeAndLongitudeToOrdersColumns() throws NoSuchFieldException {
    Map<String, String> columns = deliveryAddressColumnOverrides();

    assertEquals("delivery_address_latitude", columns.get("latitude"));
    assertEquals("delivery_address_longitude", columns.get("longitude"));
  }

  @Test
  public void shouldMapEveryAddressFieldToADeliveryAddressColumn() throws NoSuchFieldException {
    Map<String, String> expected = new HashMap<>();
    for (Field field : Address.class.getDeclaredFields()) {
      if (!Modifier.isStatic(field.getModifiers())) {
        expected.put(field.getName(), "delivery_address_" + field.getName());
      }
    }

    assertEquals(expected, deliveryAddressColumnOverrides());
  }

  private Map<String, String> deliveryAddressColumnOverrides() throws NoSuchFieldException {
    Field deliveryAddress = DeliveryInformation.class.getDeclaredField("deliveryAddress");
    Map<String, String> columns = new HashMap<>();
    for (AttributeOverride override : deliveryAddress.getAnnotation(AttributeOverrides.class).value()) {
      columns.put(override.name(), override.column().name());
    }
    return columns;
  }
}
