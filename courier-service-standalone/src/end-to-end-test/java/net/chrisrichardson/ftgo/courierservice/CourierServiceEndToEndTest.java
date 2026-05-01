package net.chrisrichardson.ftgo.courierservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.MoneyModule;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.courierservice.api.CourierAvailability;
import net.chrisrichardson.ftgo.courierservice.api.CreateCourierRequest;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class CourierServiceEndToEndTest {

  private static final String HOST = System.getenv("SERVICE_HOST") != null ? System.getenv("SERVICE_HOST") : "localhost";
  private static final int PORT = System.getenv("SERVICE_PORT") != null ? Integer.parseInt(System.getenv("SERVICE_PORT")) : 8082;
  private static ObjectMapper objectMapper = new ObjectMapper();

  private String baseUrl(String path, String... pathElements) {
    StringBuilder sb = new StringBuilder("http://");
    sb.append(HOST);
    sb.append(":");
    sb.append(PORT);
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
  public void shouldCreateCourierUpdateAvailabilityAndRetrieve() {
    int courierId = createCourier();

    noteCourierAvailable(courierId);

    verifyCourierAvailable(courierId);
  }

  private int createCourier() {
    int courierId = given().
            body(new CreateCourierRequest(new PersonName("John", "Doe"), new Address("1 Scenic Drive", null, "Oakland", "CA", "94555"))).
            contentType("application/json").
            when().
            post(baseUrl("couriers")).
            then().
            statusCode(200)
            .extract()
            .path("id");

    assertNotNull(courierId);
    assertThat(courierId).isGreaterThan(0);
    return courierId;
  }

  private void noteCourierAvailable(long courierId) {
    given().
            body(new CourierAvailability(true)).
            contentType("application/json").
            when().
            post(baseUrl("couriers", Long.toString(courierId), "availability")).
            then().
            statusCode(200);
  }

  private void verifyCourierAvailable(long courierId) {
    boolean available = given().
            when().
            get(baseUrl("couriers", Long.toString(courierId))).
            then().
            statusCode(200)
            .extract()
            .path("available");

    assertThat(available).isTrue();
  }
}
