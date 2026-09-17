package net.chrisrichardson.ftgo.consumerservice.api.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.chrisrichardson.ftgo.common.PersonName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// JUnit 5 (Jupiter) tests that the consumer API DTOs round-trip through the Jackson version shipped by
// the Spring Boot 3.5 BOM. CreateConsumerRequest only has a private no-arg constructor, so this also
// guards Jackson's ability to instantiate it when a client POSTs to the consumer service.
public class ConsumerDtoSerializationTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void shouldSerializeCreateConsumerRequest() throws Exception {
    CreateConsumerRequest request = new CreateConsumerRequest(new PersonName("Chris", "Richardson"));

    String json = objectMapper.writeValueAsString(request);

    assertThat(json).isEqualTo("{\"name\":{\"firstName\":\"Chris\",\"lastName\":\"Richardson\"}}");
  }

  @Test
  public void shouldDeserializeCreateConsumerRequest() throws Exception {
    CreateConsumerRequest request = objectMapper.readValue(
            "{\"name\":{\"firstName\":\"Chris\",\"lastName\":\"Richardson\"}}", CreateConsumerRequest.class);

    assertThat(request.getName().getFirstName()).isEqualTo("Chris");
    assertThat(request.getName().getLastName()).isEqualTo("Richardson");
  }

  @Test
  public void shouldRoundTripCreateConsumerResponse() throws Exception {
    CreateConsumerResponse response = new CreateConsumerResponse(101L);

    String json = objectMapper.writeValueAsString(response);
    CreateConsumerResponse deserialized = objectMapper.readValue(json, CreateConsumerResponse.class);

    assertThat(json).isEqualTo("{\"consumerId\":101}");
    assertThat(deserialized.getConsumerId()).isEqualTo(101L);
  }
}
