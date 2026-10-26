package net.chrisrichardson.ftgo.restaurantservice.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantResponseTest {

  @Test
  void shouldCreateGetRestaurantResponse() {
    GetRestaurantResponse response = new GetRestaurantResponse(1L, "Ajanta");

    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getName()).isEqualTo("Ajanta");
  }

  @Test
  void shouldSupportGetRestaurantResponseSetters() {
    GetRestaurantResponse response = new GetRestaurantResponse();
    response.setId(2L);
    response.setName("Updated");

    assertThat(response.getId()).isEqualTo(2L);
    assertThat(response.getName()).isEqualTo("Updated");
  }

  @Test
  void shouldCreateCreateRestaurantResponse() {
    CreateRestaurantResponse response = new CreateRestaurantResponse(1L);

    assertThat(response.getId()).isEqualTo(1L);
  }

  @Test
  void shouldSupportCreateRestaurantResponseSetters() {
    CreateRestaurantResponse response = new CreateRestaurantResponse();
    response.setId(3L);

    assertThat(response.getId()).isEqualTo(3L);
  }
}
