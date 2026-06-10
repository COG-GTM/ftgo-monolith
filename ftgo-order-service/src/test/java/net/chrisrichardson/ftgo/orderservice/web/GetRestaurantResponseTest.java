package net.chrisrichardson.ftgo.orderservice.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GetRestaurantResponseTest {

  @Test
  void shouldCreateGetRestaurantResponse() {
    GetRestaurantResponse response = new GetRestaurantResponse(1L);
    assertThat(response.getRestaurantId()).isEqualTo(1L);
  }

  @Test
  void shouldSupportSetters() {
    GetRestaurantResponse response = new GetRestaurantResponse(1L);
    response.setRestaurantId(2L);
    assertThat(response.getRestaurantId()).isEqualTo(2L);
  }
}
