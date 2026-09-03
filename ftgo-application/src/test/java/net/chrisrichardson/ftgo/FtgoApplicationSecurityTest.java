package net.chrisrichardson.ftgo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FtgoApplicationMain.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "ftgo.security.api.username=test-api",
                "ftgo.security.api.password=test-api-pw",
                "ftgo.security.operator.username=test-operator",
                "ftgo.security.operator.password=test-operator-pw"})
public class FtgoApplicationSecurityTest {

  @LocalServerPort
  private int port;

  @Value("${ftgo.security.api.username}")
  private String apiUsername;

  @Value("${ftgo.security.api.password}")
  private String apiPassword;

  @Value("${ftgo.security.operator.username}")
  private String operatorUsername;

  @Value("${ftgo.security.operator.password}")
  private String operatorPassword;

  private String url(String path) {
    return "http://localhost:" + port + path;
  }

  @Test
  public void shouldRejectUnauthenticatedCourierRead() {
    given().when().get(url("/couriers/1")).then().statusCode(401);
  }

  @Test
  public void shouldRejectUnauthenticatedCourierLocationUpdate() {
    given().contentType("application/json").body("{\"latitude\":1.0,\"longitude\":2.0}")
            .when().post(url("/couriers/1/location")).then().statusCode(401);
  }

  @Test
  public void shouldRejectUnauthenticatedTrackingRead() {
    given().when().get(url("/api/tracking/logs")).then().statusCode(401);
  }

  @Test
  public void shouldRejectApiUserReadingTrackingLogs() {
    given().auth().preemptive().basic(apiUsername, apiPassword)
            .when().get(url("/api/tracking/logs")).then().statusCode(403);
  }

  @Test
  public void shouldAllowOperatorToReadTrackingLogs() {
    given().auth().preemptive().basic(operatorUsername, operatorPassword)
            .when().get(url("/api/tracking/logs")).then().statusCode(200);
  }

  @Test
  public void shouldRecordTrackingReadsInAccessLog() {
    given().auth().preemptive().basic(operatorUsername, operatorPassword)
            .when().get(url("/api/tracking/logs")).then().statusCode(200);
    given().auth().preemptive().basic(operatorUsername, operatorPassword)
            .queryParam("uri", "/api/tracking/logs")
            .when().get(url("/api/tracking/logs/search")).then().statusCode(200)
            .body("requestUri", hasItem("/api/tracking/logs"));
  }

  @Test
  public void shouldAllowApiUserToCreateCourierAndUpdateLocation() {
    int courierId = given().auth().preemptive().basic(apiUsername, apiPassword)
            .contentType("application/json")
            .body("{\"name\":{\"firstName\":\"Jane\",\"lastName\":\"Doe\"}," +
                    "\"address\":{\"street1\":\"1 Main St\",\"city\":\"Oakland\",\"state\":\"CA\",\"zip\":\"94619\"}}")
            .when().post(url("/couriers")).then().statusCode(200)
            .extract().path("id");
    given().auth().preemptive().basic(apiUsername, apiPassword)
            .contentType("application/json").body("{\"latitude\":1.0,\"longitude\":2.0}")
            .when().post(url("/couriers/" + courierId + "/location")).then().statusCode(200);
  }

  @Test
  public void shouldAllowUnauthenticatedHealthCheck() {
    given().when().get(url("/actuator/health")).then().statusCode(200);
  }
}
