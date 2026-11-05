package net.chrisrichardson.ftgobaseline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.courierservice.api.*;
import net.chrisrichardson.ftgo.orderservice.api.events.OrderLineItemDTO;
import net.chrisrichardson.ftgo.orderservice.api.web.*;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecordDtoJsonTest {
  private final ObjectMapper mapper = new ObjectMapper()
          .registerModule(new MoneyModule())
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  private record Example(Class<?> type, String json, String defaults) {
  }

  private final List<Example> examples = List.of(
          new Example(CreateConsumerRequest.class, "{\"name\":{\"firstName\":\"A\",\"lastName\":\"B\"}}", "{\"name\":null}"),
          new Example(CreateCourierRequest.class, "{\"name\":null,\"address\":null}", "{\"name\":null,\"address\":null}"),
          new Example(CreateCourierResponse.class, "{\"id\":12}", "{\"id\":0}"),
          new Example(CourierAvailability.class, "{\"available\":true}", "{\"available\":false}"),
          new Example(CourierLocationUpdate.class, "{\"latitude\":12.5,\"longitude\":-23.5}", "{\"latitude\":0.0,\"longitude\":0.0}"),
          new Example(CourierWorkloadResponse.class, "{\"courierId\":12,\"activeDeliveries\":2,\"available\":true,\"currentLatitude\":12.5,\"currentLongitude\":-23.5,\"lastLocationUpdate\":\"2026-01-02T03:04:05\"}", "{\"courierId\":0,\"activeDeliveries\":0,\"available\":false,\"currentLatitude\":null,\"currentLongitude\":null,\"lastLocationUpdate\":null}"),
          new Example(CreateOrderRequest.class, "{\"consumerId\":1,\"restaurantId\":2,\"lineItems\":[{\"menuItemId\":\"x\",\"quantity\":3}]}", "{\"consumerId\":0,\"restaurantId\":0,\"lineItems\":null}"),
          new Example(CreateOrderRequest.LineItem.class, "{\"menuItemId\":\"x\",\"quantity\":3}", "{\"menuItemId\":null,\"quantity\":0}"),
          new Example(CreateOrderResponse.class, "{\"orderId\":12}", "{\"orderId\":0}"),
          new Example(ReviseOrderRequest.class, "{\"revisedLineItemQuantities\":{\"x\":3}}", "{\"revisedLineItemQuantities\":null}"),
          new Example(OrderAcceptance.class, "{\"readyBy\":\"2026-01-02T03:04:05\"}", "{\"readyBy\":null}"),
          new Example(OrderLineItemDTO.class, "{\"quantity\":3,\"menuItemId\":\"x\",\"name\":\"Lunch\"}", "{\"quantity\":0,\"menuItemId\":null,\"name\":null}"),
          new Example(CreateRestaurantRequest.class, "{\"name\":\"Cafe\",\"address\":null,\"menu\":{\"menuItemDTOs\":[]}}", "{\"name\":null,\"address\":null,\"menu\":null}")
  );

  @Test
  void preservesPropertiesAndRoundTrips() throws Exception {
    for (Example example : examples) {
      Object value = mapper.readValue(example.json(), example.type());
      assertEquals(mapper.readTree(example.json()), mapper.readTree(mapper.writeValueAsString(value)), example.type().getName());
    }
    CreateOrderRequest order = new CreateOrderRequest(1, 2, List.of());
    assertEquals(1, order.consumerId());
    assertEquals(2, order.restaurantId());
  }

  @Test
  void missingAndExplicitNullFieldsKeepBeanDefaults() throws Exception {
    for (Example example : examples) {
      assertEquals(mapper.readTree(example.defaults()), mapper.readTree(mapper.writeValueAsString(mapper.readValue("{}", example.type()))), example.type().getName());
      var nulls = mapper.readTree(example.defaults());
      var names = nulls.fieldNames();
      while (names.hasNext()) {
        ((com.fasterxml.jackson.databind.node.ObjectNode) nulls).putNull(names.next());
      }
      assertEquals(mapper.readTree(example.defaults()), mapper.readTree(mapper.writeValueAsString(mapper.treeToValue(nulls, example.type()))), example.type().getName());
    }
  }

  @Test
  void invalidPrimitiveAndDateInputsAreRejected() {
    assertThrows(com.fasterxml.jackson.core.JsonProcessingException.class,
            () -> mapper.readValue("{\"latitude\":\"not a number\"}", CourierLocationUpdate.class));
    assertThrows(com.fasterxml.jackson.core.JsonProcessingException.class,
            () -> mapper.readValue("{\"readyBy\":\"not a date\"}", OrderAcceptance.class));
  }
}
