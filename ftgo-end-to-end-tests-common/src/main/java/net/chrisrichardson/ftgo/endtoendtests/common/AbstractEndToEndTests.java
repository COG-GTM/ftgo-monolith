package net.chrisrichardson.ftgo.endtoendtests.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import io.eventuate.util.test.async.Eventually;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.common.security.AuthTokenService;
import net.chrisrichardson.ftgo.common.security.AuthenticatedPrincipal;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Collections;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractEndToEndTests {

  public static final String CHICKED_VINDALOO_MENU_ITEM_ID = "1";
  public static final String RESTAURANT_NAME = "My Restaurant";
  private static final Address RESTAURANT_ADDRESS = new Address("1 High Street", null, "Oakland", "CA", "94619");

  private final int revisedQuantityOfChickenVindaloo = 10;
  private int consumerId;
  private int restaurantId;
  private int orderId;
  private final Money priceOfChickenVindaloo = new Money("12.34");
  private static ObjectMapper objectMapper = new ObjectMapper();
  private int courierId;
  private int assignedCourierId;

  private String baseUrl(int port, String path, String... pathElements) {
    assertNotNull("host", getHost());
    StringBuilder sb = new StringBuilder("http://");
    sb.append(getHost());
    sb.append(":");
    sb.append(port);
    sb.append("/");
    sb.append(path);

    for (String pe : pathElements) {
      sb.append("/");
      sb.append(pe);
    }
    String s = sb.toString();
    System.out.println("url=" + s);
    return s;
  }

  private String consumerBaseUrl(String... pathElements) {
    return baseUrl(getApplicationPort(), "consumers", pathElements);
  }

  private String restaurantBaseUrl(String... pathElements) {
    return baseUrl(getApplicationPort(), "restaurants", pathElements);
  }

  private String orderBaseUrl(String... pathElements) {
    return baseUrl(getApplicationPort(), "orders", pathElements);
  }

  @BeforeClass
  public static void initialize() {
    objectMapper.registerModule(new MoneyModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
            (aClass, s) -> objectMapper
    ));

  }

  @Test
  public void shouldCreateReviseAndCancelOrder() {

    createOrder();

    reviseOrder();

    cancelOrder();

  }

  @Test
  public void shouldDeliverOrder() {

    createOrder();

    createCourier();

    noteCourierAvailable();

    acceptOrder();

    assertOrderAssignedToCourier();

    startPreparingOrder();

    orderReadyforPickup();

    pickupOrder();

    deliverOrder();

  }

  @Test
  public void shouldRejectUnauthenticatedOrderWrites() {
    createOrder();

    String createRequest = createOrderRequest(consumerId, restaurantId);
    for (String authorization : new String[]{null, "Bearer garbage"}) {
      assertEquals(401, postOrder("", createRequest, authorization));
      for (String action : new String[]{"cancel", "revise", "accept", "preparing", "ready", "pickedup", "delivered"}) {
        assertEquals(action, 401, postOrder(orderId + "/" + action, bodyForOrderAction(action), authorization));
      }
    }

    assertOrderState(orderId, "APPROVED");
  }

  @Test
  public void shouldRejectOrderWritesByWrongPrincipal() {
    consumerId = createConsumer();
    int otherConsumerId = createConsumer();
    restaurantId = createRestaurant();
    orderId = createOrder(consumerId, restaurantId);

    assertEquals(403, postOrder(orderId + "/cancel", "{}", token(AuthenticatedPrincipal.consumer(otherConsumerId))));
    assertEquals(403, postOrder(orderId + "/revise",
            "{\"revisedLineItemQuantities\":{\"1\":4}}", token(AuthenticatedPrincipal.consumer(otherConsumerId))));
    assertEquals(403, postOrder("", createOrderRequest(otherConsumerId, restaurantId),
            token(AuthenticatedPrincipal.consumer(consumerId))));
    assertEquals(403, postOrder(orderId + "/accept", acceptanceRequest(),
            token(AuthenticatedPrincipal.restaurant(restaurantId + 1000000))));
    assertEquals(403, postOrder(orderId + "/accept", acceptanceRequest(),
            token(AuthenticatedPrincipal.consumer(consumerId))));
    assertOrderState(orderId, "APPROVED");

    createCourier();
    noteCourierAvailable();
    acceptOrder();
    assignedCourierId = readAssignedCourierId(orderId);
    assertEquals(403, postOrder(orderId + "/pickedup", "{}", token(AuthenticatedPrincipal.courier(999999999L))));
    assertEquals(403, postOrder(orderId + "/delivered", "{}", token(AuthenticatedPrincipal.courier(999999999L))));
  }

  private void reviseOrder() {
    reviseOrder(orderId);
    verifyOrderRevised(orderId);
  }

  private void verifyOrderRevised(int orderId) {
    Eventually.eventually(String.format("verifyOrderRevised state %s", orderId), () -> {
      String orderTotal = given().
              when().
              get(baseUrl(getApplicationPort(), "orders", Integer.toString(orderId))).
              then().
              statusCode(200)
              .extract().
                      path("orderTotal");
      assertEquals(priceOfChickenVindaloo.multiply(revisedQuantityOfChickenVindaloo).asString(), orderTotal);
    });
    Eventually.eventually(String.format("verifyOrderRevised state %s", orderId), () -> {
      String state = given().
              when().
              get(orderBaseUrl(Integer.toString(orderId))).
              then().
              statusCode(200)
              .extract().
                      path("state");
      assertEquals("APPROVED", state);
    });
  }

  private void reviseOrder(int orderId) {
    given().
            header("Authorization", "Bearer " + token(AuthenticatedPrincipal.consumer(consumerId))).
            body(new ReviseOrderRequest(Collections.singletonMap(CHICKED_VINDALOO_MENU_ITEM_ID, revisedQuantityOfChickenVindaloo)))
            .contentType("application/json").
            when().
            post(orderBaseUrl(Integer.toString(orderId), "revise")).
            then().
            statusCode(200);
  }


  private void createOrder() {
    consumerId = createConsumer();

    restaurantId = createRestaurant();

    verifyRestaurantCreated(restaurantId);

    orderId = createOrder(consumerId, restaurantId);

    verifyOrderAuthorized(orderId);

    verifyOrderHistoryUpdated(orderId, consumerId);
  }

  private void cancelOrder() {
    cancelOrder(orderId);

    verifyOrderCancelled(orderId);
  }

  private void verifyOrderCancelled(int orderId) {
    Eventually.eventually(String.format("verifyOrderCancelled %s", orderId), () -> {
      String state = given().
              when().
              get(orderBaseUrl(Integer.toString(orderId))).
              then().
              statusCode(200)
              .extract().
                      path("state");
      assertEquals("CANCELLED", state);
    });

  }

  private void cancelOrder(int orderId) {
    given().
            header("Authorization", "Bearer " + token(AuthenticatedPrincipal.consumer(consumerId))).
            body("{}").
            contentType("application/json").
            when().
            post(orderBaseUrl(Integer.toString(orderId), "cancel")).
            then().
            statusCode(200);

  }

  private Integer createConsumer() {
    Integer consumerId =
            given().
                    body(new CreateConsumerRequest(new PersonName("John", "Doe"))).
                    contentType("application/json").
                    when().
                    post(consumerBaseUrl()).
                    then().
                    statusCode(200).
                    extract().
                    path("consumerId");

    assertNotNull(consumerId);
    return consumerId;
  }

  private int createRestaurant() {
    Integer restaurantId =
            given().
                    body(new CreateRestaurantRequest(RESTAURANT_NAME,
                            RESTAURANT_ADDRESS,
                            new RestaurantMenuDTO(Collections.singletonList(new MenuItemDTO(CHICKED_VINDALOO_MENU_ITEM_ID, "Chicken Vindaloo", priceOfChickenVindaloo))))).
                    contentType("application/json").
                    when().
                    post(restaurantBaseUrl()).
                    then().
                    statusCode(200).
                    extract().
                    path("id");

    assertNotNull(restaurantId);
    return restaurantId;
  }

  private void verifyRestaurantCreated(int restaurantId) {
    Eventually.eventually(String.format("verifyRestaurantCreated %s", restaurantId), () ->
            given().
                    when().
                    get(restaurantBaseUrl(Integer.toString(restaurantId))).
                    then().
                    statusCode(200));
  }

  private int createOrder(int consumerId, int restaurantId) {
    Integer orderId =
            given().
                    header("Authorization", "Bearer " + token(AuthenticatedPrincipal.consumer(consumerId))).
                    body(new CreateOrderRequest(consumerId, restaurantId, Collections.singletonList(new CreateOrderRequest.LineItem(CHICKED_VINDALOO_MENU_ITEM_ID, 5)))).
                    contentType("application/json").
                    when().
                    post(orderBaseUrl()).
                    then().
                    statusCode(200).
                    extract().
                    path("orderId");

    assertNotNull(orderId);
    return orderId;
  }

  private void verifyOrderAuthorized(int orderId) {
    Eventually.eventually(String.format("verifyOrderApproved %s", orderId), () -> {
      String state = given().
              when().
              get(orderBaseUrl(Integer.toString(orderId))).
              then().
              statusCode(200)
              .extract().
                      path("state");
      assertEquals("APPROVED", state);
    });
  }

  private void verifyOrderHistoryUpdated(int orderId, int consumerId) {
    Eventually.eventually(String.format("verifyOrderHistoryUpdated %s", orderId), () -> {
      String state = given().
              when().
              get(orderBaseUrl() + "?consumerId=" + consumerId).
              then().
              statusCode(200)
              .body("[0].restaurantName", equalTo(RESTAURANT_NAME))
              .extract().
                      path("[0].state");
      assertNotNull(state);
    });
  }

  private void createCourier() {
    courierId = given().
            body(new CreateCourierRequest(new PersonName("John", "Doe"), new Address("1 Scenic Drive", null, "Oakland", "CA", "94555"))).
            contentType("application/json").
            when().
            post(baseUrl(getApplicationPort(), "couriers")).
            then().
            statusCode(200)
            .extract()
            .path("id");
  }

  private void noteCourierAvailable() {
    given().
            body(new CourierAvailability(true)).
            contentType("application/json").
            when().
            post(baseUrl(getApplicationPort(), "couriers", Long.toString(courierId), "availability")).
            then().
            statusCode(200);
  }

  private void acceptOrder() {
    given().
            header("Authorization", "Bearer " + token(AuthenticatedPrincipal.restaurant(restaurantId))).
            body(new OrderAcceptance(LocalDateTime.now().plusHours(9))).
            contentType("application/json").
            when().
            post(orderBaseUrl(Long.toString(orderId), "accept")).
            then().
            statusCode(200);
  }

  private void assertOrderAssignedToCourier() {
    assignedCourierId = Eventually.eventuallyReturning(() -> readAssignedCourierId(orderId));

    given().
            when().
            get(baseUrl(getApplicationPort(), "couriers", Long.toString(assignedCourierId))).
            then().
            statusCode(200)
            .body("plan.actions[0].type", equalTo("PICKUP"))
            .body("plan.actions[1].type", equalTo("DROPOFF"));

  }

  private void startPreparingOrder() {
    given().
            header("Authorization", "Bearer " + token(AuthenticatedPrincipal.restaurant(restaurantId))).
            when().
            post(orderBaseUrl(Long.toString(orderId), "preparing")).
            then().
            statusCode(200);
  }

  private void orderReadyforPickup() {
    given().
            header("Authorization", "Bearer " + token(AuthenticatedPrincipal.restaurant(restaurantId))).
            when().
            post(orderBaseUrl(Long.toString(orderId), "ready")).
            then().
            statusCode(200);
  }

  private void pickupOrder() {
    given().
            header("Authorization", "Bearer " + token(AuthenticatedPrincipal.courier(assignedCourierId))).
            when().
            post(orderBaseUrl(Long.toString(orderId), "pickedup")).
            then().
            statusCode(200);
  }

  private void deliverOrder() {
    given().
            header("Authorization", "Bearer " + token(AuthenticatedPrincipal.courier(assignedCourierId))).
            when().
            post(orderBaseUrl(Long.toString(orderId), "delivered")).
            then().
            statusCode(200);
  }

  private int readAssignedCourierId(int orderId) {
    int assignedCourier = given().
            when().
            get(orderBaseUrl(Integer.toString(orderId))).
            then().
            statusCode(200)
            .body("courierActions[0].type", equalTo("PICKUP"))
            .body("courierActions[1].type", equalTo("DROPOFF"))
            .extract()
            .path("assignedCourier");
    assertThat(assignedCourier).isGreaterThan(0);
    return assignedCourier;
  }

  private void assertOrderState(int orderId, String expectedState) {
    given().
            when().
            get(orderBaseUrl(Integer.toString(orderId))).
            then().
            statusCode(200)
            .body("state", equalTo(expectedState));
  }

  private int postOrder(String path, String body, String authorization) {
    com.jayway.restassured.specification.RequestSpecification request = given()
            .contentType("application/json")
            .body(body);
    if (authorization != null) {
      request.header("Authorization", authorization.startsWith("Bearer ")
              ? authorization : "Bearer " + authorization);
    }
    String url = orderBaseUrl() + (path.isEmpty() ? "" : "/" + path);
    return request.when().post(url).then().extract().statusCode();
  }

  private String createOrderRequest(int consumerId, int restaurantId) {
    return "{\"consumerId\":" + consumerId + ",\"restaurantId\":" + restaurantId +
            ",\"lineItems\":[{\"menuItemId\":\"" + CHICKED_VINDALOO_MENU_ITEM_ID + "\",\"quantity\":5}]}";
  }

  private String bodyForOrderAction(String action) {
    if ("revise".equals(action)) {
      return "{\"revisedLineItemQuantities\":{\"1\":4}}";
    }
    if ("accept".equals(action)) {
      return acceptanceRequest();
    }
    return "{}";
  }

  private String acceptanceRequest() {
    return "{\"readyBy\":\"" + LocalDateTime.now().plusHours(9) + "\"}";
  }

  private String token(AuthenticatedPrincipal principal) {
    return getAuthTokenService().issue(principal, Duration.ofHours(2));
  }

  public abstract String getHost();

  public abstract int getApplicationPort();

  protected abstract AuthTokenService getAuthTokenService();
}
