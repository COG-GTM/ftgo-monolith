package net.chrisrichardson.ftgo.restaurantservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.restassured.RestAssured.given;
import static net.chrisrichardson.ftgo.restaurantservice.RestaurantTestData.AJANTA_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Boots the complete restaurant service ({@link RestaurantServiceConfiguration}: Spring MVC, Spring Data
 * JPA on Hibernate 6, actuator and the shared springdoc configuration) on Spring Boot 3.5 / Java 21 and
 * exercises it over real HTTP with io.rest-assured 5. This is the migration smoke test: it fails if any
 * auto-configuration, jakarta.* mapping or springdoc setup is broken, and it checks that a restaurant
 * created through the REST API is persisted (including its element-collection menu) and readable again.
 *
 * <p>The database is an in-memory H2 in MySQL mode with a Hibernate-generated schema because the real
 * Flyway migrations are MySQL-specific (same set-up as ftgo-domain's entity mapping test).
 */
@SpringBootTest(classes = RestaurantServiceConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:ftgo-restaurant-service;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
                // Trust the declared MySQL 8 dialect instead of H2's JDBC metadata.
                "spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false",
                "spring.jpa.properties.jakarta.persistence.database-product-name=MySQL",
                "spring.jpa.properties.jakarta.persistence.database-major-version=8",
                "spring.jpa.properties.jakarta.persistence.database-minor-version=0",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.flyway.enabled=false"
        })
public class RestaurantServiceApplicationTest {

  @LocalServerPort
  private int port;

  // The application's own mapper (MoneyModule registered by CommonJsonMapperInitializer), so the test
  // sends exactly the JSON a real client would.
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  // Point rest-assured at the embedded Tomcat that Boot started on a random port.
  @BeforeEach
  public void setUp() {
    RestAssured.port = port;
  }

  @Test
  public void shouldCreateAndFetchRestaurant() throws Exception {
    // Create the restaurant through the REST API and pick up the generated id.
    long restaurantId = given().
            contentType(ContentType.JSON).
            body(objectMapper.writeValueAsString(RestaurantTestData.ajantaCreateRequest())).
    when().
            post("/restaurants").
    then().
            statusCode(200).
            body("id", greaterThan(0)).
    extract().
            jsonPath().getLong("id");

    // Read it back via GET; the response carries the id and name that were persisted.
    given().
    when().
            get("/restaurants/{restaurantId}", restaurantId).
    then().
            statusCode(200).
            body("id", equalTo((int) restaurantId)).
            body("name", equalTo(AJANTA_NAME));

    // Hibernate 6 must have written the @ElementCollection menu into the collection table the
    // Flyway schema defines (restaurant_menu_items), one row per menu item.
    Integer menuRows = jdbcTemplate.queryForObject(
            "select count(*) from restaurant_menu_items where restaurant_id = ?", Integer.class, restaurantId);
    assertEquals(2, menuRows);
  }

  @Test
  public void shouldReturn404ForUnknownRestaurant() {
    given().
    when().
            get("/restaurants/{restaurantId}", Long.MAX_VALUE).
    then().
            statusCode(404);
  }

  @Test
  public void shouldPublishOpenApiDocumentationForRestaurantEndpoints() {
    // springdoc (via common-swagger) documents the controller under the "ftgo" group.
    given().
    when().
            get("/v3/api-docs/ftgo").
    then().
            statusCode(200).
            body("info.title", equalTo("FTGO")).
            body("paths", hasKey("/restaurants")).
            body("paths", hasKey("/restaurants/{restaurantId}"));
  }

  @Test
  public void shouldExposeActuatorHealth() {
    // Actuator on Boot 3 lives under /actuator; the DataSource health contributor must see H2 as UP.
    given().
    when().
            get("/actuator/health").
    then().
            statusCode(200).
            body("status", equalTo("UP"));
  }
}
