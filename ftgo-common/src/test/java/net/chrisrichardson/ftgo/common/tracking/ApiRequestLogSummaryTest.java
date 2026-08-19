package net.chrisrichardson.ftgo.common.tracking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApiRequestLogSummaryTest {

  @Test
  public void shouldNotSerializeSensitiveFields() throws Exception {
    ApiRequestLog log = new ApiRequestLog("corr-1", "GET", "/orders",
            "token=secret&ssn=123-45-6789", "203.0.113.7", "curl/7.64");
    log.complete(500, 12, "could not execute statement: password=hunter2");

    String json = new ObjectMapper().registerModule(new JavaTimeModule())
            .writeValueAsString(ApiRequestLogSummary.of(log));

    assertFalse(json.contains("secret"));
    assertFalse(json.contains("123-45-6789"));
    assertFalse(json.contains("curl/7.64"));
    assertFalse(json.contains("hunter2"));
    assertFalse(json.contains("203.0.113.7"));
    assertTrue(json.contains("\"failed\":true"));
    assertTrue(json.contains("203.0.x.x"));
  }

  @Test
  public void shouldMaskAddresses() {
    assertEquals("203.0.x.x", ApiRequestLogSummary.maskAddress("203.0.113.7"));
    assertEquals("2001:db8:x:x", ApiRequestLogSummary.maskAddress("2001:db8:1234:5678"));
    assertEquals("x", ApiRequestLogSummary.maskAddress("not-an-address"));
  }
}
