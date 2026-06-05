package net.chrisrichardson.ftgo.orderservice.web;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GetRestaurantResponseTest {

  @Test
  public void shouldCreateWithId() {
    GetRestaurantResponse response = new GetRestaurantResponse(42L);
    assertThat(response.getRestaurantId()).isEqualTo(42L);
  }

  @Test
  public void shouldSetAndGetRestaurantId() {
    GetRestaurantResponse response = new GetRestaurantResponse(1L);
    response.setRestaurantId(99L);
    assertThat(response.getRestaurantId()).isEqualTo(99L);
  }
}
