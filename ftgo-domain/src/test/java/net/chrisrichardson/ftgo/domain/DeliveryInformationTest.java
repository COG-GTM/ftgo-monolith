package net.chrisrichardson.ftgo.domain;

import org.junit.Test;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class DeliveryInformationTest {

  @Test
  public void shouldMapDeliveryAddressCoordinatesToDeliveryColumns() throws NoSuchFieldException {
    AttributeOverrides overrides = DeliveryInformation.class.getDeclaredField("deliveryAddress")
            .getAnnotation(AttributeOverrides.class);
    Map<String, String> columns = Arrays.stream(overrides.value())
            .collect(Collectors.toMap(AttributeOverride::name, override -> override.column().name()));

    assertEquals("delivery_address_latitude", columns.get("latitude"));
    assertEquals("delivery_address_longitude", columns.get("longitude"));
  }
}
