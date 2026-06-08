package net.chrisrichardson.ftgo.restaurantmicroservice;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.ReviseMenuRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Boots the standalone Restaurant microservice against its in-memory H2 database
 * (schema created by Flyway) and exercises the REST API contract end to end.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RestaurantMicroserviceMain.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestaurantMicroserviceIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  private CreateRestaurantRequest sampleRequest() {
    Address address = new Address("1 High Street", null, "Oakland", "CA", "94619", 37.8044, -122.2712);
    RestaurantMenuDTO menu = new RestaurantMenuDTO(Collections.singletonList(
            new MenuItemDTO("1", "Chicken Vindaloo", new Money("12.34"))));
    return new CreateRestaurantRequest("My Restaurant", address, menu);
  }

  @Test
  public void shouldCreateAndGetRestaurant() {
    ResponseEntity<RestaurantDTO> created =
            restTemplate.postForEntity("/restaurants", sampleRequest(), RestaurantDTO.class);
    assertEquals(HttpStatus.OK, created.getStatusCode());

    Long id = created.getBody().getId();
    assertNotNull(id);

    ResponseEntity<RestaurantDTO> fetched =
            restTemplate.getForEntity("/restaurants/" + id, RestaurantDTO.class);
    assertEquals(HttpStatus.OK, fetched.getStatusCode());

    RestaurantDTO body = fetched.getBody();
    assertEquals("My Restaurant", body.getName());
    assertEquals("Oakland", body.getAddress().getCity());
    assertEquals(1, body.getMenuItems().size());
    assertEquals("Chicken Vindaloo", body.getMenuItems().get(0).getName());
    assertEquals(new Money("12.34"), body.getMenuItems().get(0).getPrice());
  }

  @Test
  public void shouldReturnNotFoundForUnknownRestaurant() {
    ResponseEntity<String> response = restTemplate.getForEntity("/restaurants/9999", String.class);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void shouldReviseMenu() {
    Long id = restTemplate.postForEntity("/restaurants", sampleRequest(), RestaurantDTO.class).getBody().getId();

    RestaurantMenuDTO revisedMenu = new RestaurantMenuDTO(Arrays.asList(
            new MenuItemDTO("1", "Chicken Tikka Masala", new Money("13.99")),
            new MenuItemDTO("2", "Garlic Naan", new Money("3.50"))));

    restTemplate.put("/restaurants/" + id + "/menu", new ReviseMenuRequest(revisedMenu));

    RestaurantDTO body = restTemplate.getForEntity("/restaurants/" + id, RestaurantDTO.class).getBody();
    assertEquals(2, body.getMenuItems().size());
    assertEquals("Chicken Tikka Masala", body.getMenuItems().get(0).getName());
  }
}
