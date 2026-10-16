package net.chrisrichardson.ftgo.orderservice.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.domain.Action;
import net.chrisrichardson.ftgo.domain.ActionType;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.orderservice.OrderDetailsMother;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderResponsesJsonTest {

  private final ObjectMapper objectMapper = new ObjectMapper()
          .registerModule(new MoneyModule())
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  @Test
  public void getOrderResponseSerializesExpectedContractFields() throws Exception {
    GetOrderResponse response = responseWithCourierActions();

    JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

    assertThat(json.get("orderId").asLong()).isEqualTo(99L);
    assertThat(json.get("state").asText()).isEqualTo("APPROVED");
    assertThat(json.get("orderTotal").asText()).isEqualTo("61.70");
    assertThat(json.get("restaurantName").asText()).isEqualTo("Ajanta");
    assertThat(json.get("assignedCourier").asLong()).isEqualTo(7L);
    assertThat(json.get("courierActions")).hasSize(2);
    assertThat(json.get("courierActions").get(0).get("type").asText()).isEqualTo(ActionType.PICKUP.name());
    assertThat(json.get("courierActions").get(0).get("time").isNull()).isTrue();
    assertThat(json.get("courierActions").get(1).get("type").asText()).isEqualTo(ActionType.DROPOFF.name());
    assertThat(json.get("courierActions").get(1).get("time").asText()).isEqualTo("2030-01-01T12:30:00");
    assertThat(json.get("estimatedDeliveryTime").asText()).isEqualTo("2030-01-01T12:30:00");
  }

  @Test
  public void getOrderResponseRoundTripsAllResponseFields() throws Exception {
    GetOrderResponse response = responseWithCourierActions();

    GetOrderResponse roundTripped = objectMapper.readValue(objectMapper.writeValueAsString(response),
            GetOrderResponse.class);

    assertThat(roundTripped.getOrderId()).isEqualTo(response.getOrderId());
    assertThat(roundTripped.getState()).isEqualTo(response.getState());
    assertThat(roundTripped.getOrderTotal()).isEqualTo(response.getOrderTotal());
    assertThat(roundTripped.getRestaurantName()).isEqualTo(response.getRestaurantName());
    assertThat(roundTripped.getAssignedCourier()).isEqualTo(response.getAssignedCourier());
    assertThat(roundTripped.getEstimatedDeliveryTime()).isEqualTo(response.getEstimatedDeliveryTime());
    assertThat(roundTripped.getCourierActions()).hasSize(2);
    for (int i = 0; i < response.getCourierActions().size(); i++) {
      assertThat(roundTripped.getCourierActions().get(i).getType())
              .isEqualTo(response.getCourierActions().get(i).getType());
      assertThat(roundTripped.getCourierActions().get(i).getTime())
              .isEqualTo(response.getCourierActions().get(i).getTime());
    }
  }

  @Test
  public void getRestaurantResponseSerializesAndDeserializesRestaurantId() throws Exception {
    GetRestaurantResponse response = new GetRestaurantResponse(5L);

    String json = objectMapper.writeValueAsString(response);

    assertThat(objectMapper.readTree(json).toString()).isEqualTo("{\"restaurantId\":5}");
    assertThat(objectMapper.readValue(json, GetRestaurantResponse.class).getRestaurantId()).isEqualTo(5L);
  }

  private GetOrderResponse responseWithCourierActions() {
    Order order = new Order(OrderDetailsMother.CONSUMER_ID,
            net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT,
            OrderDetailsMother.chickenVindalooLineItems());
    order.setId(99L);
    LocalDateTime estimatedDeliveryTime = LocalDateTime.of(2030, 1, 1, 12, 30);
    return new GetOrderResponse(99L, "APPROVED", new Money("61.70"), "Ajanta", 7L,
            Arrays.asList(Action.makePickup(order), Action.makeDropoff(order, estimatedDeliveryTime)),
            estimatedDeliveryTime);
  }
}
