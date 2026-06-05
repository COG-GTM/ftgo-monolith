package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Action;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class GetOrderResponseTest {

  @Test
  public void shouldCreateWithAllFields() {
    LocalDateTime deliveryTime = LocalDateTime.now().plusHours(1);
    GetOrderResponse response = new GetOrderResponse(1L, "APPROVED", new Money("25.00"),
            "Test Restaurant", 10L, Collections.emptyList(), deliveryTime);

    assertThat(response.getOrderId()).isEqualTo(1L);
    assertThat(response.getState()).isEqualTo("APPROVED");
    assertThat(response.getOrderTotal()).isEqualTo(new Money("25.00"));
    assertThat(response.getRestaurantName()).isEqualTo("Test Restaurant");
    assertThat(response.getAssignedCourier()).isEqualTo(10L);
    assertThat(response.getCourierActions()).isEmpty();
    assertThat(response.getEstimatedDeliveryTime()).isEqualTo(deliveryTime);
  }

  @Test
  public void shouldSetAndGetAllFields() {
    GetOrderResponse response = new GetOrderResponse(0L, null, null, null, null, null, null);
    response.setOrderId(2L);
    response.setState("CANCELLED");
    response.setOrderTotal(new Money("10.00"));
    response.setAssignedCourier(5L);
    response.setCourierActions(Collections.emptyList());
    LocalDateTime time = LocalDateTime.now();
    response.setEstimatedDeliveryTime(time);

    assertThat(response.getOrderId()).isEqualTo(2L);
    assertThat(response.getState()).isEqualTo("CANCELLED");
    assertThat(response.getOrderTotal()).isEqualTo(new Money("10.00"));
    assertThat(response.getAssignedCourier()).isEqualTo(5L);
    assertThat(response.getCourierActions()).isEmpty();
    assertThat(response.getEstimatedDeliveryTime()).isEqualTo(time);
  }
}
