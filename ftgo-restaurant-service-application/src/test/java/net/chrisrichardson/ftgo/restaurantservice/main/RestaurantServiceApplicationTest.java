package net.chrisrichardson.ftgo.restaurantservice.main;

import io.restassured.RestAssured;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;

/**
 * Exercises the extracted service as a running application: its own Spring Boot entry point,
 * its own datasource and its own Flyway-managed schema.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RestaurantServiceApplicationMain.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:ftgo_restaurant;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
        })
public class RestaurantServiceApplicationTest {

  private static final String CREATE_RESTAURANT_REQUEST_JSON =
          "{\"name\":\"My Restaurant\"," +
          "\"address\":{\"street1\":\"1 High Street\",\"city\":\"Oakland\",\"state\":\"CA\",\"zip\":\"94619\",\"latitude\":37.79,\"longitude\":-122.19}," +
          "\"menu\":{\"menuItemDTOs\":[{\"id\":\"1\",\"name\":\"Chicken Vindaloo\",\"price\":\"12.34\"}]}}";

  @LocalServerPort
  private int port;

  @Test
  public void shouldCreateAndFindRestaurant() {
    Integer restaurantId = createRestaurant();

    given().
    when().
            get(restaurantUrl(restaurantId)).
    then().
            statusCode(200).
            body("id", equalTo(restaurantId)).
            body("name", equalTo("My Restaurant")).
            body("address.city", equalTo("Oakland")).
            body("address.latitude", equalTo(37.79f)).
            body("menu.menuItemDTOs[0].id", equalTo("1")).
            body("menu.menuItemDTOs[0].name", equalTo("Chicken Vindaloo")).
            body("menu.menuItemDTOs[0].price", equalTo("12.34"));
  }

  @Test
  public void shouldReturnNotFoundForUnknownRestaurant() {
    given().
    when().
            get(restaurantUrl(99999)).
    then().
            statusCode(404);
  }

  @Test
  public void shouldReportHealthy() {
    given().
    when().
            get(baseUrl("/actuator/health")).
    then().
            statusCode(200).
            body("status", equalTo("UP"));
  }

  private Integer createRestaurant() {
    Integer restaurantId =
            given().
                    body(CREATE_RESTAURANT_REQUEST_JSON).
                    contentType("application/json").
            when().
                    post(baseUrl("/restaurants")).
            then().
                    statusCode(200).
                    extract().
                    path("id");

    assertNotNull(restaurantId);
    return restaurantId;
  }

  private String restaurantUrl(int restaurantId) {
    return baseUrl("/restaurants/" + restaurantId);
  }

  private String baseUrl(String path) {
    return RestAssured.baseURI + ":" + port + path;
  }
}
