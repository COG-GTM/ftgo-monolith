package net.chrisrichardson.ftgo.orderservice.api.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OrderAcceptanceTest {

  private static final LocalDateTime READY_BY = LocalDateTime.of(2026, 10, 20, 12, 30, 45);

  private final ObjectMapper objectMapper = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  @Test
  public void noArgConstructorLeavesReadyByNull() {
    assertNull(new OrderAcceptance().getReadyBy());
  }

  @Test
  public void constructorAndSetterHoldReadyBy() {
    OrderAcceptance acceptance = new OrderAcceptance(READY_BY);
    assertEquals(READY_BY, acceptance.getReadyBy());

    LocalDateTime later = READY_BY.plusMinutes(15);
    acceptance.setReadyBy(later);
    assertEquals(later, acceptance.getReadyBy());
  }

  @Test
  public void deserializesIsoTimestamp() throws Exception {
    OrderAcceptance acceptance = objectMapper.readValue(
            "{\"readyBy\":\"2026-10-20T12:30:45\"}", OrderAcceptance.class);

    assertEquals(READY_BY, acceptance.getReadyBy());
  }

  @Test
  public void serializesReadyByAsIsoString() throws Exception {
    JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(new OrderAcceptance(READY_BY)));

    assertEquals("2026-10-20T12:30:45", node.get("readyBy").asText());
  }
}
