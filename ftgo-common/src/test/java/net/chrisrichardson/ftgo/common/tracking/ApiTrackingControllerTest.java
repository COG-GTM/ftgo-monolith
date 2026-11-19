package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApiTrackingControllerTest {

  private ApiRequestLogRepository repository;
  private ApiTrackingController controller;
  private MockMvc mvc;

  @Before
  public void setUp() {
    repository = mock(ApiRequestLogRepository.class);
    when(repository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
    when(repository.findErrorsSince(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
    when(repository.findByRequestUri(any(String.class))).thenReturn(Collections.emptyList());
    controller = new ApiTrackingController(repository);
    mvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  public void preservesDefaultAndLegacyMinutesOnEveryEndpoint() throws Exception {
    String[] inputs = {null, "", "30", " 30", "+30", "030", "0x1E", "#1E", "-5"};
    int[] expected = {60, 60, 30, 30, 30, 30, 30, 30, -5};
    for (String endpoint : new String[]{"/logs", "/logs/errors", "/stats"}) {
      for (int i = 0; i < inputs.length; i++) {
        reset(repository);
        when(repository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(repository.findErrorsSince(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        LocalDateTime before = LocalDateTime.now().minusMinutes(expected[i]);
        MockHttpServletRequestBuilder request = get("/api/tracking" + endpoint);
        if (inputs[i] != null) {
          request.param("minutesBack", inputs[i]);
        }
        mvc.perform(request).andExpect(status().isOk());
        LocalDateTime after = LocalDateTime.now().minusMinutes(expected[i]);
        ArgumentCaptor<LocalDateTime> since = ArgumentCaptor.forClass(LocalDateTime.class);
        if (endpoint.equals("/logs/errors")) {
          verify(repository).findErrorsSince(since.capture());
        } else {
          verify(repository).findRecentLogs(since.capture());
        }
        assertFalse(since.getValue().isBefore(before));
        assertFalse(since.getValue().isAfter(after));
      }
    }
  }

  @Test
  public void repeatedMinutesUseFirstValue() throws Exception {
    mvc.perform(get("/api/tracking/stats").param("minutesBack", "30", "abc"))
            .andExpect(status().isOk());
    Map<String, Object> stats = controller.getStats(minutes("30", "abc")).getBody();
    assertEquals(Integer.valueOf(30), stats.get("periodMinutes"));
  }

  @Test
  public void rejectsInvalidAndOverflowingMinutesBeforeQuerying() {
    for (String input : new String[]{"abc", "1.5", "99999999999", "30,40"}) {
      assertMinutesIllegalArgument(() -> controller.getRecentLogs(minutes(input)));
      assertMinutesIllegalArgument(() -> controller.getErrors(minutes(input)));
      assertMinutesIllegalArgument(() -> controller.getStats(minutes(input)));
    }
    verifyZeroInteractions(repository);
  }

  @Test
  public void repeatedEmptyFirstMinutesAreRejectedBeforeQuerying() {
    assertMinutesIllegalArgument(() -> controller.getRecentLogs(minutes("", "2")));
    assertMinutesIllegalArgument(() -> controller.getErrors(minutes("", "2")));
    assertMinutesIllegalArgument(() -> controller.getStats(minutes("", "2")));
    verifyZeroInteractions(repository);
  }

  @Test
  public void missingUriIsRejectedBeforeQuerying() {
    assertIllegalArgument(() -> controller.searchByUri(null));
    verifyZeroInteractions(repository);
  }

  @Test
  public void emptyAndKnownUriPassThroughUnchanged() throws Exception {
    mvc.perform(get("/api/tracking/logs/search").param("uri", ""))
            .andExpect(status().isOk());
    verify(repository).findByRequestUri("");
    mvc.perform(get("/api/tracking/logs/search").param("uri", "/api/tracking"))
            .andExpect(status().isOk());
    verify(repository).findByRequestUri("/api/tracking");
  }

  @Test
  public void correlationIdLookupIsUnchanged() {
    ApiRequestLog log = new ApiRequestLog();
    when(repository.findByCorrelationId("known")).thenReturn(log);
    assertSame(log, controller.getByCorrelationId("known").getBody());
    assertEquals(HttpStatus.OK, controller.getByCorrelationId("known").getStatusCode());
    assertEquals(HttpStatus.NOT_FOUND, controller.getByCorrelationId("unknown").getStatusCode());
  }

  private void assertIllegalArgument(Runnable action) {
    try {
      action.run();
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // Expected validation failure.
    }
  }

  private MultiValueMap<String, String> minutes(String... values) {
    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    for (String value : values) {
      parameters.add("minutesBack", value);
    }
    return parameters;
  }

  private void assertMinutesIllegalArgument(Runnable action) {
    try {
      action.run();
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertEquals("minutesBack must be an integer", expected.getMessage());
    }
  }
}
