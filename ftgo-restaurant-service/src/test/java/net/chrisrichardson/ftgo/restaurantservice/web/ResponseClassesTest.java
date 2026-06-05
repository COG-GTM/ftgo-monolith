package net.chrisrichardson.ftgo.restaurantservice.web;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseClassesTest {

  @Test
  public void shouldCreateRestaurantResponse() {
    CreateRestaurantResponse response = new CreateRestaurantResponse(1L);
    assertThat(response.getId()).isEqualTo(1L);
  }

  @Test
  public void shouldSetRestaurantResponseId() {
    CreateRestaurantResponse response = new CreateRestaurantResponse();
    response.setId(99L);
    assertThat(response.getId()).isEqualTo(99L);
  }

  @Test
  public void shouldCreateGetRestaurantResponse() {
    GetRestaurantResponse response = new GetRestaurantResponse(1L, "Italian");
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getName()).isEqualTo("Italian");
  }

  @Test
  public void shouldSetGetRestaurantResponseFields() {
    GetRestaurantResponse response = new GetRestaurantResponse();
    response.setId(5L);
    response.setName("Mexican");
    assertThat(response.getId()).isEqualTo(5L);
    assertThat(response.getName()).isEqualTo("Mexican");
  }
}
