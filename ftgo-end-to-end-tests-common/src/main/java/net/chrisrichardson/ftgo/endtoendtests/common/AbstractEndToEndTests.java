package net.chrisrichardson.ftgo.endtoendtests.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.specification.RequestSpecification;
import io.eventuate.util.test.async.Eventually;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.consumerservice.api.web.CreateConsumerRequest;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CourierLocationUpdate;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.CreateOrderRequest;
import net.chrisrichardson.ftgo.orderservice.api.web.OrderAcceptance;
import net.chrisrichardson.ftgo.orderservice.api.web.ReviseOrderRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.CreateRestaurantRequest;
import net.chrisrichardson.ftgo.restaurantservice.events.MenuItemDTO;
import net.chrisrichardson.ftgo.restaurantservice.events.RestaurantMenuDTO;
import net.chrisrichardson.ftgo.security.CreateAccountRequest;
import net.chrisrichardson.ftgo.security.Role;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractEndToEndTests {

  public static final String CHICKED_VINDALOO_MENU_ITEM_ID = "1";
  public static final String RESTAURANT_NAME = "My Restaurant";
  private static final Address RESTAURANT_ADDRESS = new Address("1 High Street", null, "Oakland", "CA", "94619");
  private static final String ENTITY_PASSWORD = "test-password";

  private final int revisedQuantityOfChickenVindaloo = 10;
  private final Money priceOfChickenVindaloo = new Money("12.34");
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private int consumerId;
  private int restaurantId;
  private int orderId;
  private int courierId;
  private String consumerUsername;
  private String consumerPassword;
  private String restaurantUsername;
  private String restaurantPassword;
  private String courierUsername;
  private String courierPassword;

  private String baseUrl(int port, String path, String... pathElements) {
    assertNotNull("host", getHost());
    StringBuilder sb = new StringBuilder("http://");
    sb.append(getHost()).append(":").append(port).append("/").append(path);
    for (String pathElement : pathElements) {
      sb.append("/").append(pathElement);
    }
    return sb.toString();
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

  private String courierBaseUrl(String... pathElements) {
    return baseUrl(getApplicationPort(), "couriers", pathElements);
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
    createConsumerAndRestaurant();
    orderId = createOrder(consumerId, restaurantId, consumerUsername, consumerPassword);

    reviseOrder(orderId);
    verifyOrderRevised(orderId);
    cancelOrder(orderId);
    verifyOrderCancelled(orderId);
  }

  @Test
  public void shouldDeliverOrder() {
    createConsumerAndRestaurant();
    courierId = createCourier();
    createCourierAccount(courierId);
    orderId = createOrder(consumerId, restaurantId, consumerUsername, consumerPassword);

    noteCourierAvailable(courierId, courierUsername, courierPassword);
    acceptOrder(orderId, restaurantUsername, restaurantPassword);
    assertOrderAssignedToCourier(orderId, courierId);
    int otherCourierId = createCourier();
    String otherCourierUsername = uniqueUsername("other-courier");
    String otherCourierPassword = ENTITY_PASSWORD;
    createAccount(otherCourierUsername, otherCourierPassword, Role.COURIER, otherCourierId);
    auth(otherCourierUsername, otherCourierPassword).when()
            .post(orderBaseUrl(Integer.toString(orderId), "pickedup")).then().statusCode(403);
    auth(otherCourierUsername, otherCourierPassword).when()
            .post(orderBaseUrl(Integer.toString(orderId), "delivered")).then().statusCode(403);
    startPreparingOrder(orderId, restaurantUsername, restaurantPassword);
    orderReadyForPickup(orderId, restaurantUsername, restaurantPassword);
    pickupOrder(orderId, courierUsername, courierPassword);
    deliverOrder(orderId, courierUsername, courierPassword);
  }

  @Test
  public void shouldEnforceAuthenticationAndOwnership() {
    createConsumerAndRestaurant();
    int consumerAId = consumerId;
    int restaurantAId = restaurantId;
    String consumerAUsername = consumerUsername;
    String consumerAPassword = consumerPassword;
    String restaurantAUsername = restaurantUsername;
    String restaurantAPassword = restaurantPassword;
    int orderAId = createOrder(consumerAId, restaurantAId, consumerAUsername, consumerAPassword);

    int consumerBId = createConsumer();
    int restaurantBId = createRestaurant();
    String consumerBUsername = uniqueUsername("consumer-b");
    String consumerBPassword = ENTITY_PASSWORD;
    String restaurantBUsername = uniqueUsername("restaurant-b");
    String restaurantBPassword = ENTITY_PASSWORD;
    createAccount(consumerBUsername, consumerBPassword, Role.CONSUMER, consumerBId);
    createAccount(restaurantBUsername, restaurantBPassword, Role.RESTAURANT, restaurantBId);
    operator().body(new CreateAccountRequest(consumerAUsername, consumerAPassword, Role.CONSUMER, (long) consumerAId))
            .contentType("application/json").when().post(baseUrl(getApplicationPort(), "accounts")).then().statusCode(409);

    int courierAId = createCourier();
    String courierAUsername = uniqueUsername("courier-a");
    String courierAPassword = ENTITY_PASSWORD;
    createAccount(courierAUsername, courierAPassword, Role.COURIER, courierAId);
    int courierBId = createCourier();
    String courierBUsername = uniqueUsername("courier-b");
    String courierBPassword = ENTITY_PASSWORD;
    createAccount(courierBUsername, courierBPassword, Role.COURIER, courierBId);

    assertUnauthenticatedRequestsAreRejected(orderAId, courierAId);
    given().auth().preemptive().basic(consumerAUsername, "wrong-password")
            .when().get(orderBaseUrl(Integer.toString(orderAId))).then().statusCode(401);

    given().auth().preemptive().basic(consumerBUsername, consumerBPassword)
            .when().get(orderBaseUrl(Integer.toString(orderAId))).then().statusCode(403);
    given().auth().preemptive().basic(consumerBUsername, consumerBPassword)
            .body("{}").contentType("application/json")
            .when().post(orderBaseUrl(Integer.toString(orderAId), "cancel")).then().statusCode(403);
    given().auth().preemptive().basic(consumerBUsername, consumerBPassword)
            .body(new ReviseOrderRequest(Collections.singletonMap(CHICKED_VINDALOO_MENU_ITEM_ID, revisedQuantityOfChickenVindaloo)))
            .contentType("application/json")
            .when().post(orderBaseUrl(Integer.toString(orderAId), "revise")).then().statusCode(403);
    given().auth().preemptive().basic(consumerBUsername, consumerBPassword)
            .body(orderRequest(consumerAId, restaurantAId)).contentType("application/json")
            .when().post(orderBaseUrl()).then().statusCode(403);
    given().auth().preemptive().basic(consumerBUsername, consumerBPassword)
            .when().get(orderBaseUrl() + "?consumerId=" + consumerAId).then().statusCode(403);

    given().auth().preemptive().basic(consumerAUsername, consumerAPassword)
            .when().get(orderBaseUrl(Integer.toString(orderAId))).then().statusCode(200)
            .body("state", equalTo("APPROVED"))
            .body("orderTotal", equalTo(priceOfChickenVindaloo.multiply(5).asString()));
    given().auth().preemptive().basic(restaurantBUsername, restaurantBPassword)
            .body(new OrderAcceptance(java.time.LocalDateTime.now().plusHours(9))).contentType("application/json")
            .when().post(orderBaseUrl(Integer.toString(orderAId), "accept")).then().statusCode(403);
    given().auth().preemptive().basic(courierBUsername, courierBPassword)
            .when().post(orderBaseUrl(Integer.toString(orderAId), "pickedup")).then().statusCode(403);

    given().auth().preemptive().basic(courierAUsername, courierAPassword)
            .body(new CourierAvailability(true)).contentType("application/json")
            .when().post(courierBaseUrl(Integer.toString(courierBId), "availability")).then().statusCode(403);
    given().auth().preemptive().basic(courierAUsername, courierAPassword)
            .body(new CourierLocationUpdate(0, 0)).contentType("application/json")
            .when().post(courierBaseUrl(Integer.toString(courierBId), "location")).then().statusCode(403);
    given().auth().preemptive().basic(courierAUsername, courierAPassword)
            .when().get(courierBaseUrl(Integer.toString(courierBId))).then().statusCode(403);
    given().auth().preemptive().basic(courierAUsername, courierAPassword)
            .when().get(courierBaseUrl(Integer.toString(courierBId), "workload")).then().statusCode(403);
    given().auth().preemptive().basic(consumerAUsername, consumerAPassword)
            .body(new CreateCourierRequest(new PersonName("Not", "Allowed"), RESTAURANT_ADDRESS))
            .contentType("application/json").when().post(courierBaseUrl()).then().statusCode(403);
    given().auth().preemptive().basic(consumerAUsername, consumerAPassword)
            .body(new CreateAccountRequest(uniqueUsername("unauthorized"), ENTITY_PASSWORD, Role.CONSUMER, (long) consumerAId))
            .contentType("application/json").when().post(baseUrl(getApplicationPort(), "accounts")).then().statusCode(403);

    given().auth().preemptive().basic(courierAUsername, courierAPassword)
            .body(new CourierLocationUpdate(500, 0)).contentType("application/json")
            .when().post(courierBaseUrl(Integer.toString(courierAId), "location")).then().statusCode(400);
    given().auth().preemptive().basic(courierAUsername, courierAPassword)
            .body(new CourierLocationUpdate(0, -181)).contentType("application/json")
            .when().post(courierBaseUrl(Integer.toString(courierAId), "location")).then().statusCode(400);
    given().auth().preemptive().basic(courierAUsername, courierAPassword)
            .body(new CourierLocationUpdate(0, 0)).contentType("application/json")
            .when().post(courierBaseUrl(Integer.toString(courierAId), "location")).then().statusCode(200);

    operator().when().get(courierBaseUrl("999999999")).then().statusCode(404);
    operator().when().get(orderBaseUrl("999999999")).then().statusCode(404);
    operator().body("{}").contentType("application/json")
            .when().post(orderBaseUrl("999999999", "cancel")).then().statusCode(404);
  }

  private void createConsumerAndRestaurant() {
    consumerId = createConsumer();
    consumerUsername = uniqueUsername("consumer");
    consumerPassword = ENTITY_PASSWORD;
    createAccount(consumerUsername, consumerPassword, Role.CONSUMER, consumerId);

    restaurantId = createRestaurant();
    restaurantUsername = uniqueUsername("restaurant");
    restaurantPassword = ENTITY_PASSWORD;
    createAccount(restaurantUsername, restaurantPassword, Role.RESTAURANT, restaurantId);
    verifyRestaurantCreated(restaurantId);
  }

  private void assertUnauthenticatedRequestsAreRejected(int orderId, int courierId) {
    given().when().get(orderBaseUrl(Integer.toString(orderId))).then().statusCode(401);
    given().body("{}").contentType("application/json")
            .when().post(orderBaseUrl(Integer.toString(orderId), "cancel")).then().statusCode(401);
    given().body(orderRequest(consumerId, restaurantId)).contentType("application/json")
            .when().post(orderBaseUrl()).then().statusCode(401);
    given().when().get(courierBaseUrl(Integer.toString(courierId))).then().statusCode(401);
    given().when().get(courierBaseUrl(Integer.toString(courierId), "workload")).then().statusCode(401);
    given().body(new CourierLocationUpdate(0, 0)).contentType("application/json")
            .when().post(courierBaseUrl(Integer.toString(courierId), "location")).then().statusCode(401);
    given().body(new CourierAvailability(true)).contentType("application/json")
            .when().post(courierBaseUrl(Integer.toString(courierId), "availability")).then().statusCode(401);
    given().body(new CreateCourierRequest(new PersonName("Unauth", "Allowed"), RESTAURANT_ADDRESS))
            .contentType("application/json").when().post(courierBaseUrl()).then().statusCode(401);
  }

  private int createConsumer() {
    Integer id = given().body(new CreateConsumerRequest(new PersonName("John", "Doe")))
            .contentType("application/json").when().post(consumerBaseUrl()).then().statusCode(200)
            .extract().path("consumerId");
    assertNotNull(id);
    return id;
  }

  private int createRestaurant() {
    Integer id = given().body(restaurantRequest()).contentType("application/json")
            .when().post(restaurantBaseUrl()).then().statusCode(200).extract().path("id");
    assertNotNull(id);
    return id;
  }

  private CreateRestaurantRequest restaurantRequest() {
    return new CreateRestaurantRequest(RESTAURANT_NAME, RESTAURANT_ADDRESS,
            new RestaurantMenuDTO(Collections.singletonList(new MenuItemDTO(
                    CHICKED_VINDALOO_MENU_ITEM_ID, "Chicken Vindaloo", priceOfChickenVindaloo))));
  }

  private void createAccount(String username, String password, Role role, long entityId) {
    operator().body(new CreateAccountRequest(username, password, role, entityId))
            .contentType("application/json").when().post(baseUrl(getApplicationPort(), "accounts"))
            .then().statusCode(200).body("username", equalTo(username));
  }

  private String uniqueUsername(String prefix) {
    return prefix + "-" + UUID.randomUUID();
  }

  private int createOrder(int consumerId, int restaurantId, String username, String password) {
    Integer id = auth(username, password).body(orderRequest(consumerId, restaurantId))
            .contentType("application/json").when().post(orderBaseUrl()).then().statusCode(200)
            .extract().path("orderId");
    assertNotNull(id);
    verifyOrderAuthorized(id, username, password);
    verifyOrderHistoryUpdated(id, consumerId, username, password);
    return id;
  }

  private CreateOrderRequest orderRequest(int consumerId, int restaurantId) {
    return new CreateOrderRequest(consumerId, restaurantId,
            Collections.singletonList(new CreateOrderRequest.LineItem(CHICKED_VINDALOO_MENU_ITEM_ID, 5)));
  }

  private void verifyRestaurantCreated(int restaurantId) {
    Eventually.eventually(String.format("verifyRestaurantCreated %s", restaurantId), () ->
            given().when().get(restaurantBaseUrl(Integer.toString(restaurantId))).then().statusCode(200));
  }

  private void verifyOrderAuthorized(int orderId, String username, String password) {
    Eventually.eventually(String.format("verifyOrderApproved %s", orderId), () -> {
      String state = auth(username, password).when().get(orderBaseUrl(Integer.toString(orderId)))
              .then().statusCode(200).extract().path("state");
      assertEquals("APPROVED", state);
    });
  }

  private void verifyOrderHistoryUpdated(int orderId, int consumerId, String username, String password) {
    Eventually.eventually(String.format("verifyOrderHistoryUpdated %s", orderId), () -> {
      String state = auth(username, password).when().get(orderBaseUrl() + "?consumerId=" + consumerId)
              .then().statusCode(200).body("[0].restaurantName", equalTo(RESTAURANT_NAME))
              .extract().path("[0].state");
      assertNotNull(state);
    });
  }

  private int createCourier() {
    Integer id = operator().body(new CreateCourierRequest(new PersonName("John", "Doe"),
                    new Address("1 Scenic Drive", null, "Oakland", "CA", "94555")))
            .contentType("application/json").when().post(courierBaseUrl())
            .then().statusCode(200).extract().path("id");
    assertNotNull(id);
    return id;
  }

  private void createCourierAccount(int courierId) {
    courierUsername = uniqueUsername("courier");
    courierPassword = ENTITY_PASSWORD;
    createAccount(courierUsername, courierPassword, Role.COURIER, courierId);
  }

  private void noteCourierAvailable(int courierId, String username, String password) {
    auth(username, password).body(new CourierAvailability(true)).contentType("application/json")
            .when().post(courierBaseUrl(Integer.toString(courierId), "availability")).then().statusCode(200);
  }

  private void acceptOrder(int orderId, String username, String password) {
    auth(username, password).body(new OrderAcceptance(java.time.LocalDateTime.now().plusHours(9)))
            .contentType("application/json").when().post(orderBaseUrl(Integer.toString(orderId), "accept"))
            .then().statusCode(200);
  }

  private void assertOrderAssignedToCourier(int orderId, int expectedCourierId) {
    int assignedCourier = Eventually.eventuallyReturning(() -> {
      Integer id = auth(consumerUsername, consumerPassword).when().get(orderBaseUrl(Integer.toString(orderId)))
              .then().statusCode(200)
              .body("courierActions[0].type", equalTo("PICKUP"))
              .body("courierActions[1].type", equalTo("DROPOFF"))
              .extract().path("assignedCourier");
      assertThat(id).isGreaterThan(0);
      return id;
    });
    assertEquals(expectedCourierId, assignedCourier);

    Map courier = auth(courierUsername, courierPassword).when()
            .get(courierBaseUrl(Integer.toString(expectedCourierId))).then().statusCode(200)
            .body("plan.actions[0].type", equalTo("PICKUP"))
            .body("plan.actions[1].type", equalTo("DROPOFF"))
            .extract().as(Map.class);
    assertThat(courier.keySet()).containsExactlyInAnyOrder("id", "name", "address", "available",
            "currentLatitude", "currentLongitude", "lastLocationUpdate", "plan");
    Map name = (Map) courier.get("name");
    Map address = (Map) courier.get("address");
    Map plan = (Map) courier.get("plan");
    assertThat(name.keySet()).containsExactlyInAnyOrder("firstName", "lastName");
    assertThat(address.keySet()).containsExactlyInAnyOrder(
            "street1", "street2", "city", "state", "zip", "latitude", "longitude");
    assertThat(plan.keySet()).containsExactly("actions");
    Map action = (Map) ((java.util.List) plan.get("actions")).get(0);
    assertThat(action.keySet()).containsExactlyInAnyOrder("type", "orderId", "time");
  }

  private void startPreparingOrder(int orderId, String username, String password) {
    auth(username, password).when().post(orderBaseUrl(Integer.toString(orderId), "preparing")).then().statusCode(200);
  }

  private void orderReadyForPickup(int orderId, String username, String password) {
    auth(username, password).when().post(orderBaseUrl(Integer.toString(orderId), "ready")).then().statusCode(200);
  }

  private void pickupOrder(int orderId, String username, String password) {
    auth(username, password).when().post(orderBaseUrl(Integer.toString(orderId), "pickedup")).then().statusCode(200);
  }

  private void deliverOrder(int orderId, String username, String password) {
    auth(username, password).when().post(orderBaseUrl(Integer.toString(orderId), "delivered")).then().statusCode(200);
  }

  private void reviseOrder(int orderId) {
    auth(consumerUsername, consumerPassword)
            .body(new ReviseOrderRequest(Collections.singletonMap(CHICKED_VINDALOO_MENU_ITEM_ID, revisedQuantityOfChickenVindaloo)))
            .contentType("application/json").when().post(orderBaseUrl(Integer.toString(orderId), "revise"))
            .then().statusCode(200);
  }

  private void verifyOrderRevised(int orderId) {
    Eventually.eventually(String.format("verifyOrderRevised state %s", orderId), () -> {
      String total = auth(consumerUsername, consumerPassword).when().get(orderBaseUrl(Integer.toString(orderId)))
              .then().statusCode(200).extract().path("orderTotal");
      assertEquals(priceOfChickenVindaloo.multiply(revisedQuantityOfChickenVindaloo).asString(), total);
    });
    Eventually.eventually(String.format("verifyOrderRevised state %s", orderId), () -> {
      String state = auth(consumerUsername, consumerPassword).when().get(orderBaseUrl(Integer.toString(orderId)))
              .then().statusCode(200).extract().path("state");
      assertEquals("APPROVED", state);
    });
  }

  private void cancelOrder(int orderId) {
    auth(consumerUsername, consumerPassword).body("{}").contentType("application/json")
            .when().post(orderBaseUrl(Integer.toString(orderId), "cancel")).then().statusCode(200);
  }

  private void verifyOrderCancelled(int orderId) {
    Eventually.eventually(String.format("verifyOrderCancelled %s", orderId), () -> {
      String state = auth(consumerUsername, consumerPassword).when().get(orderBaseUrl(Integer.toString(orderId)))
              .then().statusCode(200).extract().path("state");
      assertEquals("CANCELLED", state);
    });
  }

  private RequestSpecification auth(String username, String password) {
    return given().auth().preemptive().basic(username, password);
  }

  private RequestSpecification operator() {
    return auth(getOperatorUsername(), getOperatorPassword());
  }

  public abstract String getHost();

  public abstract int getApplicationPort();

  public abstract String getOperatorUsername();

  public abstract String getOperatorPassword();
}
