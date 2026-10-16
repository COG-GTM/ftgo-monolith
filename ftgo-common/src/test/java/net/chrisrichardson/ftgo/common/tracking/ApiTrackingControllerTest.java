package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApiTrackingControllerTest {

  private ApiRequestLogRepository repository;
  private MockMvc mockMvc;

  @Before
  public void setUp() {
    repository = mock(ApiRequestLogRepository.class);
    mockMvc = MockMvcBuilders.standaloneSetup(new ApiTrackingController(repository)).build();
  }

  @Test
  public void getRecentLogsDefaultsToSixtyMinutes() throws Exception {
    when(repository.findRecentLogs(any(LocalDateTime.class)))
            .thenReturn(Collections.singletonList(log("GET", "/orders", 200, 10L)));
    LocalDateTime around = LocalDateTime.now();

    mockMvc.perform(get("/api/tracking/logs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].correlationId").value("correlation-GET-/orders"));

    ArgumentCaptor<LocalDateTime> since = ArgumentCaptor.forClass(LocalDateTime.class);
    verify(repository).findRecentLogs(since.capture());
    assertSinceIsNear(since.getValue(), around, 60);
  }

  @Test
  public void getRecentLogsUsesRequestedMinutesBack() throws Exception {
    when(repository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Collections.<ApiRequestLog>emptyList());
    LocalDateTime around = LocalDateTime.now();

    mockMvc.perform(get("/api/tracking/logs").param("minutesBack", "5"))
            .andExpect(status().isOk());

    ArgumentCaptor<LocalDateTime> since = ArgumentCaptor.forClass(LocalDateTime.class);
    verify(repository).findRecentLogs(since.capture());
    assertSinceIsNear(since.getValue(), around, 5);
  }

  @Test
  public void invalidMinutesBackReturnsBadRequestWithoutCallingRepository() throws Exception {
    mockMvc.perform(get("/api/tracking/logs").param("minutesBack", "abc"))
            .andExpect(status().isBadRequest());

    verifyZeroInteractions(repository);
  }

  @Test
  public void getErrorsCallsErrorQuery() throws Exception {
    when(repository.findErrorsSince(any(LocalDateTime.class)))
            .thenReturn(Collections.singletonList(log("GET", "/failed", 500, 10L)));
    LocalDateTime around = LocalDateTime.now();

    mockMvc.perform(get("/api/tracking/logs/errors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].responseStatus").value(500));

    ArgumentCaptor<LocalDateTime> since = ArgumentCaptor.forClass(LocalDateTime.class);
    verify(repository).findErrorsSince(since.capture());
    verify(repository, never()).findByCorrelationId(anyString());
    assertSinceIsNear(since.getValue(), around, 60);
  }

  @Test
  public void searchByUriCallsRepositoryAndReturnsBody() throws Exception {
    when(repository.findByRequestUri("orders"))
            .thenReturn(Collections.singletonList(log("GET", "/orders", 200, 10L)));

    mockMvc.perform(get("/api/tracking/logs/search").param("uri", "orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].requestUri").value("/orders"));

    verify(repository).findByRequestUri("orders");
  }

  @Test
  public void searchRequiresUri() throws Exception {
    mockMvc.perform(get("/api/tracking/logs/search"))
            .andExpect(status().isBadRequest());

    verifyZeroInteractions(repository);
  }

  @Test
  public void getByCorrelationIdReturnsFoundLog() throws Exception {
    when(repository.findByCorrelationId("abc-123")).thenReturn(log("GET", "/orders", 200, 10L));

    mockMvc.perform(get("/api/tracking/logs/abc-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correlationId").value("correlation-GET-/orders"));

    verify(repository).findByCorrelationId("abc-123");
  }

  @Test
  public void getByCorrelationIdReturnsNotFoundWithEmptyBody() throws Exception {
    when(repository.findByCorrelationId("missing")).thenReturn(null);

    mockMvc.perform(get("/api/tracking/logs/missing"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));

    verify(repository).findByCorrelationId("missing");
  }

  @Test
  public void statsWithNoLogsReturnsZeroValuesAndEmptyMaps() throws Exception {
    when(repository.findRecentLogs(any(LocalDateTime.class)))
            .thenReturn(Collections.<ApiRequestLog>emptyList());

    mockMvc.perform(get("/api/tracking/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRequests").value(0))
            .andExpect(jsonPath("$.periodMinutes").value(60))
            .andExpect(jsonPath("$.errorCount").value(0))
            .andExpect(jsonPath("$.errorRate").value(0.0))
            .andExpect(jsonPath("$.avgDurationMs").value(0.0))
            .andExpect(jsonPath("$.p95DurationMs").value(0))
            .andExpect(jsonPath("$.statusCodeDistribution").value(equalTo(Collections.emptyMap())))
            .andExpect(jsonPath("$.topEndpoints").value(equalTo(Collections.emptyMap())));
  }

  @Test
  public void statsSummarizesCountsAndDurations() throws Exception {
    when(repository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Arrays.asList(
            log("GET", "/a", 200, 10L),
            log("GET", "/a", 201, 20L),
            log("POST", "/b", 404, 30L),
            log("GET", "/c", 500, 40L)));

    mockMvc.perform(get("/api/tracking/stats").param("minutesBack", "30"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRequests").value(4))
            .andExpect(jsonPath("$.periodMinutes").value(30))
            .andExpect(jsonPath("$.errorCount").value(2))
            .andExpect(jsonPath("$.errorRate").value(0.5))
            .andExpect(jsonPath("$.avgDurationMs").value(25.0))
            .andExpect(jsonPath("$.p95DurationMs").value(40))
            .andExpect(jsonPath("$.statusCodeDistribution['200']").value(1))
            .andExpect(jsonPath("$.statusCodeDistribution['201']").value(1))
            .andExpect(jsonPath("$.statusCodeDistribution['404']").value(1))
            .andExpect(jsonPath("$.statusCodeDistribution['500']").value(1))
            .andExpect(jsonPath("$.topEndpoints['GET /a']").value(2))
            .andExpect(jsonPath("$.topEndpoints['POST /b']").value(1))
            .andExpect(jsonPath("$.topEndpoints['GET /c']").value(1));
  }

  @Test
  public void statsRoundsAverageDurationToTwoDecimalPlaces() throws Exception {
    when(repository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Arrays.asList(
            log("GET", "/a", 200, 1L),
            log("GET", "/b", 200, 2L),
            log("GET", "/c", 200, 2L)));

    mockMvc.perform(get("/api/tracking/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.avgDurationMs").value(1.67));
  }

  @Test
  public void statsIncludeUncompletedLogsInRequestAndEndpointCountsOnly() throws Exception {
    ApiRequestLog uncompleted = new ApiRequestLog("uncompleted", "GET", "/b", null, null, null);
    when(repository.findRecentLogs(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(log("GET", "/a", 500, 100L), uncompleted));

    mockMvc.perform(get("/api/tracking/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRequests").value(2))
            .andExpect(jsonPath("$.errorCount").value(1))
            .andExpect(jsonPath("$.errorRate").value(0.5))
            .andExpect(jsonPath("$.avgDurationMs").value(100.0))
            .andExpect(jsonPath("$.statusCodeDistribution['500']").value(1))
            .andExpect(jsonPath("$.topEndpoints['GET /a']").value(1))
            .andExpect(jsonPath("$.topEndpoints['GET /b']").value(1));
  }

  @Test
  public void statsP95CurrentlyReturnsZeroWhenSomeDurationsAreMissing() throws Exception {
    // Suspected bug: p95 skip count uses logs.size() instead of the number of logs with durations, so missing durations push p95 to 0.
    ApiRequestLog uncompleted = new ApiRequestLog("uncompleted", "GET", "/b", null, null, null);
    when(repository.findRecentLogs(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(log("GET", "/a", 200, 100L), uncompleted));

    mockMvc.perform(get("/api/tracking/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.p95DurationMs").value(0));
  }

  @Test
  public void statsP95CurrentlyReturnsNinetySixthValueForHundredSamples() throws Exception {
    // Suspected bug: p95 skips floor(n*0.95) elements, an off-by-one vs nearest-rank (expected 95).
    List<ApiRequestLog> logs = new ArrayList<>();
    for (int duration = 1; duration <= 100; duration++) {
      logs.add(log("GET", "/sample", 200, (long) duration));
    }
    when(repository.findRecentLogs(any(LocalDateTime.class))).thenReturn(logs);

    mockMvc.perform(get("/api/tracking/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.p95DurationMs").value(96));
  }

  @Test
  public void statsCurrentlyKeysEndpointWithNullMethodAndExcludesNullUri() throws Exception {
    // Suspected bug: null httpMethod produces a "null /x" endpoint key.
    ApiRequestLog nullMethod = log(null, "/x", 200, 1L);
    ApiRequestLog nullUri = log("GET", null, 200, 1L);
    when(repository.findRecentLogs(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(nullMethod, nullUri));

    mockMvc.perform(get("/api/tracking/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.topEndpoints").value(hasKey("null /x")))
            .andExpect(jsonPath("$.topEndpoints").value(not(hasKey("GET null"))));
  }

  @Test
  public void negativeMinutesBackCurrentlyAcceptedAndQueriesFuture() throws Exception {
    // Suspected bug: no validation of minutesBack; negative values query from the future.
    when(repository.findRecentLogs(any(LocalDateTime.class)))
            .thenReturn(Collections.<ApiRequestLog>emptyList());
    LocalDateTime around = LocalDateTime.now();

    mockMvc.perform(get("/api/tracking/logs").param("minutesBack", "-5"))
            .andExpect(status().isOk());

    ArgumentCaptor<LocalDateTime> since = ArgumentCaptor.forClass(LocalDateTime.class);
    verify(repository).findRecentLogs(since.capture());
    LocalDateTime expectedFuture = around.plusMinutes(5);
    assertTrue(Math.abs(Duration.between(expectedFuture, since.getValue()).toMillis()) <= 5000);
    assertTrue(since.getValue().isAfter(around));
  }

  private ApiRequestLog log(String method, String uri, Integer status, Long duration) {
    ApiRequestLog log = new ApiRequestLog("correlation-" + method + "-" + uri,
            method, uri, null, "127.0.0.1", "JUnit");
    if (status != null) {
      log.complete(status, duration);
    }
    return log;
  }

  private void assertSinceIsNear(LocalDateTime actual, LocalDateTime around, int minutesBack) {
    LocalDateTime expected = around.minusMinutes(minutesBack);
    assertTrue("expected since " + expected + " but was " + actual,
            Math.abs(Duration.between(expected, actual).toMillis()) <= 5000);
  }
}
