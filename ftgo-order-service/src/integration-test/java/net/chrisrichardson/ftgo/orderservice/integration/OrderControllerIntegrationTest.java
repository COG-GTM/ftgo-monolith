package net.chrisrichardson.ftgo.orderservice.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerConfiguration;
import net.chrisrichardson.ftgo.orderservice.main.OrderServiceConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_ORDER_TOTAL;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.CHICKEN_VINDALOO_QUANTITY;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.AJANTA_RESTAURANT_NAME;
import static net.chrisrichardson.ftgo.orderservice.RestaurantMother.CHICKEN_VINDALOO_MENU_ITEM_ID;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Drives the order REST API over HTTP against an embedded server backed by MySQL.
 */
@SpringBootTest(classes = {OrderServiceConfiguration.class, ConsumerConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerIntegrationTest extends AbstractOrderFlowIntegrationTest {

  @LocalServerPort
  private int port;

  @Before
  public void configureRestAssured() {
    RestAssured.port = port;
  }

  @Test
  public void shouldPlaceAndDeliverOrderViaRestApi() {
    long consumerId = createConsumer();
    long restaurantId = createRestaurant();
    long courierId = createAvailableCourier();

    long orderId = placeOrder(consumerId, restaurantId);

    given().when().get("/orders/{orderId}", orderId).then()
            .statusCode(200)
            .body("orderId", equalTo((int) orderId))
            .body("state", equalTo("APPROVED"))
            .body("orderTotal", equalTo(CHICKEN_VINDALOO_ORDER_TOTAL.asString()))
            .body("restaurantName", equalTo(AJANTA_RESTAURANT_NAME))
            .body("assignedCourier", nullValue());

    given().contentType(ContentType.JSON)
            .body(Collections.singletonMap("readyBy", LocalDateTime.now().plusHours(1).withNano(0).toString()))
            .when().post("/orders/{orderId}/accept", orderId)
            .then().statusCode(200);

    for (String transition : new String[]{"preparing", "ready", "pickedup", "delivered"}) {
      given().when().post("/orders/{orderId}/" + transition, orderId).then().statusCode(200);
    }

    given().when().get("/orders/{orderId}", orderId).then()
            .statusCode(200)
            .body("state", equalTo("DELIVERED"))
            .body("assignedCourier", equalTo((int) courierId))
            .body("courierActions.type", contains("PICKUP", "DROPOFF"))
            .body("estimatedDeliveryTime", notNullValue());

    given().queryParam("consumerId", consumerId).when().get("/orders").then()
            .statusCode(200)
            .body("", hasSize(1))
            .body("[0].orderId", equalTo((int) orderId));

    assertEquals("DELIVERED", orderStateInDb(orderId));
    assertTrue("API requests should be tracked in api_request_log",
            jdbcTemplate.queryForObject("select count(*) from api_request_log where request_uri like '/orders%'",
                    Integer.class) >= 7);
  }

  @Test
  public void shouldCancelOrderViaRestApi() {
    long orderId = placeOrder(createConsumer(), createRestaurant());

    given().when().post("/orders/{orderId}/cancel", orderId).then()
            .statusCode(200)
            .body("state", equalTo("CANCELLED"));

    assertEquals("CANCELLED", orderStateInDb(orderId));
  }

  @Test
  public void shouldReviseOrderViaRestApi() {
    long orderId = placeOrder(createConsumer(), createRestaurant());

    given().contentType(ContentType.JSON)
            .body(Collections.singletonMap("revisedLineItemQuantities",
                    Collections.singletonMap(CHICKEN_VINDALOO_MENU_ITEM_ID, 1)))
            .when().post("/orders/{orderId}/revise", orderId)
            .then().statusCode(200)
            .body("orderTotal", equalTo("12.34"));

    assertEquals(Integer.valueOf(1), jdbcTemplate.queryForObject(
            "select quantity from order_line_items where order_id = ?", Integer.class, orderId));
  }

  @Test
  public void shouldReturn404ForUnknownOrder() {
    given().when().get("/orders/{orderId}", Long.MAX_VALUE).then().statusCode(404);
    given().when().post("/orders/{orderId}/cancel", Long.MAX_VALUE).then().statusCode(404);
  }

  private long placeOrder(long consumerId, long restaurantId) {
    Map<String, Object> lineItem = new HashMap<>();
    lineItem.put("menuItemId", CHICKEN_VINDALOO_MENU_ITEM_ID);
    lineItem.put("quantity", CHICKEN_VINDALOO_QUANTITY);

    Map<String, Object> request = new HashMap<>();
    request.put("consumerId", consumerId);
    request.put("restaurantId", restaurantId);
    request.put("lineItems", Collections.singletonList(lineItem));

    long orderId = given().contentType(ContentType.JSON).body(request)
            .when().post("/orders")
            .then().statusCode(200)
            .extract().jsonPath().getLong("orderId");

    assertEquals("APPROVED", orderStateInDb(orderId));
    return orderId;
  }
}
