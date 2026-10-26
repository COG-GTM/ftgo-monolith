package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.domain.Action;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class GetOrderResponseTest {

  @Test
  void shouldCreateGetOrderResponse() {
    LocalDateTime deliveryTime = LocalDateTime.now().plusMinutes(30);
    GetOrderResponse response = new GetOrderResponse(1L, "APPROVED", new Money("20.00"),
            "Ajanta", 42L, Collections.emptyList(), deliveryTime);

    assertThat(response.getOrderId()).isEqualTo(1L);
    assertThat(response.getState()).isEqualTo("APPROVED");
    assertThat(response.getOrderTotal()).isEqualTo(new Money("20.00"));
    assertThat(response.getRestaurantName()).isEqualTo("Ajanta");
    assertThat(response.getAssignedCourier()).isEqualTo(42L);
    assertThat(response.getCourierActions()).isEmpty();
    assertThat(response.getEstimatedDeliveryTime()).isEqualTo(deliveryTime);
  }

  @Test
  void shouldSupportSetters() {
    GetOrderResponse response = new GetOrderResponse(0L, null, null, null, null, null, null);
    response.setOrderId(2L);
    response.setState("DELIVERED");
    response.setOrderTotal(new Money("30.00"));
    response.setAssignedCourier(5L);
    LocalDateTime time = LocalDateTime.now();
    response.setEstimatedDeliveryTime(time);
    response.setCourierActions(Collections.emptyList());

    assertThat(response.getOrderId()).isEqualTo(2L);
    assertThat(response.getState()).isEqualTo("DELIVERED");
    assertThat(response.getOrderTotal()).isEqualTo(new Money("30.00"));
    assertThat(response.getAssignedCourier()).isEqualTo(5L);
    assertThat(response.getEstimatedDeliveryTime()).isEqualTo(time);
    assertThat(response.getCourierActions()).isEmpty();
  }
}
