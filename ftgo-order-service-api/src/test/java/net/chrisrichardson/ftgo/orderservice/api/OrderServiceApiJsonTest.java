package net.chrisrichardson.ftgo.orderservice.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.orderservice.api.events.OrderDetails;
import net.chrisrichardson.ftgo.orderservice.api.events.OrderLineItemDTO;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderResponse;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// JUnit 5 (Jupiter) tests that the order API DTOs round-trip through the Jackson version shipped by
// the Spring Boot 3.5 BOM. The ObjectMapper mirrors CommonJsonMapperInitializer (MoneyModule +
// JavaTimeModule, ISO-8601 dates) so the JSON shape matches what the services exchange at runtime.
public class OrderServiceApiJsonTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  // Configure the mapper once for all tests exactly as ftgo-common does for the running services.
  @BeforeAll
  public static void initialize() {
    objectMapper.registerModule(new MoneyModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  // CreateOrderRequest and its nested LineItem only have private no-arg constructors; Jackson must
  // still be able to instantiate them and populate every field via setters.
  @Test
  public void shouldRoundTripCreateOrderRequest() throws IOException {
    CreateOrderRequest request = new CreateOrderRequest(101L, 202L,
            List.of(new CreateOrderRequest.LineItem("chickenVindaloo", 2),
                    new CreateOrderRequest.LineItem("naan", 3)));

    CreateOrderRequest result = objectMapper.readValue(objectMapper.writeValueAsString(request), CreateOrderRequest.class);

    assertEquals(101L, result.getConsumerId());
    assertEquals(202L, result.getRestaurantId());
    assertEquals(2, result.getLineItems().size());
    assertEquals("chickenVindaloo", result.getLineItems().get(0).getMenuItemId());
    assertEquals(2, result.getLineItems().get(0).getQuantity());
    assertEquals("naan", result.getLineItems().get(1).getMenuItemId());
    assertEquals(3, result.getLineItems().get(1).getQuantity());
  }

  @Test
  public void shouldRoundTripCreateOrderResponse() throws IOException {
    String json = objectMapper.writeValueAsString(new CreateOrderResponse(99L));

    assertEquals("{\"orderId\":99}", json);
    assertEquals(99L, objectMapper.readValue(json, CreateOrderResponse.class).getOrderId());
  }

  @Test
  public void shouldRoundTripReviseOrderRequest() throws IOException {
    ReviseOrderRequest request = new ReviseOrderRequest(Collections.singletonMap("chickenVindaloo", 5));

    ReviseOrderRequest result = objectMapper.readValue(objectMapper.writeValueAsString(request), ReviseOrderRequest.class);

    assertEquals(Collections.singletonMap("chickenVindaloo", 5), result.getRevisedLineItemQuantities());
  }

  // LocalDateTime must be written as an ISO-8601 string (not an epoch/array timestamp) and read back.
  @Test
  public void shouldRoundTripOrderAcceptanceReadyByAsIsoString() throws IOException {
    LocalDateTime readyBy = LocalDateTime.of(2026, 9, 17, 18, 30, 0);
    String json = objectMapper.writeValueAsString(new OrderAcceptance(readyBy));

    assertEquals("{\"readyBy\":\"2026-09-17T18:30:00\"}", json);
    assertEquals(readyBy, objectMapper.readValue(json, OrderAcceptance.class).getReadyBy());
  }

  // OrderDetails embeds Money, which MoneyModule encodes as a plain string; reflection-based equals
  // from commons-lang confirms every field (including the line items) survived the round trip.
  @Test
  public void shouldRoundTripOrderDetailsWithMoneyAsString() throws IOException {
    OrderDetails details = new OrderDetails(101L, 202L,
            List.of(new OrderLineItemDTO(2, "chickenVindaloo", "Chicken Vindaloo")),
            new Money("12.34"));
    String json = objectMapper.writeValueAsString(details);

    assertEquals("{\"lineItems\":[{\"quantity\":2,\"menuItemId\":\"chickenVindaloo\",\"name\":\"Chicken Vindaloo\"}],"
            + "\"orderTotal\":\"12.34\",\"restaurantId\":202,\"consumerId\":101}", json);
    OrderDetails result = objectMapper.readValue(json, OrderDetails.class);
    assertEquals(new Money("12.34"), result.getOrderTotal());
    assertEquals("Chicken Vindaloo", result.getLineItems().get(0).getName());
    assertEquals(2, result.getLineItems().get(0).getQuantity());
    assertEquals(101L, result.getConsumerId());
    assertEquals(202L, result.getRestaurantId());
  }
}
