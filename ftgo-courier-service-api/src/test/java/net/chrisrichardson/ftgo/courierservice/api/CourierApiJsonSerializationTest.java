package net.chrisrichardson.ftgo.courierservice.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JUnit 5 (Jupiter) JSON round-trip tests for the courier service API DTOs.
 *
 * The ObjectMapper is configured the same way {@code CommonJsonMapperInitializer} configures the
 * application's mapper (JavaTimeModule, ISO-8601 dates instead of timestamps), so these tests prove
 * the DTOs still serialize as expected on the Jackson version shipped by the Spring Boot 3.5 BOM.
 */
public class CourierApiJsonSerializationTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  // Mirror the application-wide mapper settings once for all tests in this class.
  @BeforeAll
  public static void initialize() {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  // CreateCourierRequest nests the shared PersonName and Address value objects from ftgo-common.
  @Test
  public void shouldRoundTripCreateCourierRequest() throws Exception {
    CreateCourierRequest request = new CreateCourierRequest(
            new PersonName("Jane", "Doe"),
            new Address("1 Main St", null, "Oakland", "CA", "94611"));

    String json = objectMapper.writeValueAsString(request);
    JsonNode tree = objectMapper.readTree(json);
    assertThat(tree.at("/name/firstName").asText()).isEqualTo("Jane");
    assertThat(tree.at("/name/lastName").asText()).isEqualTo("Doe");
    assertThat(tree.at("/address/street1").asText()).isEqualTo("1 Main St");
    assertThat(tree.at("/address/zip").asText()).isEqualTo("94611");

    CreateCourierRequest deserialized = objectMapper.readValue(json, CreateCourierRequest.class);
    // PersonName/Address have no equals(); compare the fields that were populated.
    assertThat(deserialized.getName().getFirstName()).isEqualTo("Jane");
    assertThat(deserialized.getName().getLastName()).isEqualTo("Doe");
    assertThat(deserialized.getAddress().getStreet1()).isEqualTo("1 Main St");
    assertThat(deserialized.getAddress().getStreet2()).isNull();
    assertThat(deserialized.getAddress().getCity()).isEqualTo("Oakland");
    assertThat(deserialized.getAddress().getState()).isEqualTo("CA");
    assertThat(deserialized.getAddress().getZip()).isEqualTo("94611");
  }

  @Test
  public void shouldRoundTripCreateCourierResponse() throws Exception {
    String json = objectMapper.writeValueAsString(new CreateCourierResponse(42L));
    assertThat(json).isEqualTo("{\"id\":42}");
    assertThat(objectMapper.readValue(json, CreateCourierResponse.class).getId()).isEqualTo(42L);
  }

  // Boolean getter uses the `is` prefix; Jackson must still map it to the `available` property.
  @Test
  public void shouldRoundTripCourierAvailability() throws Exception {
    String json = objectMapper.writeValueAsString(new CourierAvailability(true));
    assertThat(json).isEqualTo("{\"available\":true}");
    assertThat(objectMapper.readValue("{\"available\":false}", CourierAvailability.class).isAvailable()).isFalse();
  }

  @Test
  public void shouldRoundTripCourierLocationUpdate() throws Exception {
    CourierLocationUpdate update = new CourierLocationUpdate(37.8044, -122.2712);

    CourierLocationUpdate deserialized =
            objectMapper.readValue(objectMapper.writeValueAsString(update), CourierLocationUpdate.class);
    assertThat(deserialized.getLatitude()).isEqualTo(37.8044);
    assertThat(deserialized.getLongitude()).isEqualTo(-122.2712);
  }

  // LocalDateTime must be written as an ISO-8601 string (not an array/timestamp) and read back intact;
  // nullable Double coordinates must survive as JSON null.
  @Test
  public void shouldRoundTripCourierWorkloadResponseWithIsoDateTime() throws Exception {
    LocalDateTime lastUpdate = LocalDateTime.of(2024, 5, 17, 13, 45, 30);
    CourierWorkloadResponse response =
            new CourierWorkloadResponse(7L, 2, true, 37.8044, -122.2712, lastUpdate);

    String json = objectMapper.writeValueAsString(response);
    JsonNode tree = objectMapper.readTree(json);
    assertThat(tree.get("lastLocationUpdate").asText()).isEqualTo("2024-05-17T13:45:30");
    assertThat(tree.get("activeDeliveries").asInt()).isEqualTo(2);

    CourierWorkloadResponse deserialized = objectMapper.readValue(json, CourierWorkloadResponse.class);
    assertThat(deserialized.getCourierId()).isEqualTo(7L);
    assertThat(deserialized.getActiveDeliveries()).isEqualTo(2);
    assertThat(deserialized.isAvailable()).isTrue();
    assertThat(deserialized.getCurrentLatitude()).isEqualTo(37.8044);
    assertThat(deserialized.getCurrentLongitude()).isEqualTo(-122.2712);
    assertThat(deserialized.getLastLocationUpdate()).isEqualTo(lastUpdate);

    // A courier with no location yet: nullable coordinates and timestamp serialize as null.
    CourierWorkloadResponse noLocation = objectMapper.readValue(
            objectMapper.writeValueAsString(new CourierWorkloadResponse(8L, 0, false, null, null, null)),
            CourierWorkloadResponse.class);
    assertThat(noLocation.getCurrentLatitude()).isNull();
    assertThat(noLocation.getCurrentLongitude()).isNull();
    assertThat(noLocation.getLastLocationUpdate()).isNull();
  }
}
