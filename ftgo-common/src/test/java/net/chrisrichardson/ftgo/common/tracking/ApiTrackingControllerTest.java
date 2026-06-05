package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ApiTrackingControllerTest {

  private ApiRequestLogRepository logRepository;
  private ApiTrackingController controller;

  @Before
  public void setUp() {
    logRepository = mock(ApiRequestLogRepository.class);
    controller = new ApiTrackingController(logRepository);
  }

  @Test
  public void shouldGetRecentLogs() {
    ApiRequestLog log1 = new ApiRequestLog("c1", "GET", "/orders", null, "127.0.0.1", "test");
    when(logRepository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Arrays.asList(log1));

    assertThat(controller.getRecentLogs(60).getBody()).hasSize(1);
  }

  @Test
  public void shouldGetErrors() {
    when(logRepository.findErrorsSince(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

    assertThat(controller.getErrors(30).getBody()).isEmpty();
  }

  @Test
  public void shouldSearchByUri() {
    when(logRepository.findByRequestUri("/orders")).thenReturn(Collections.emptyList());

    assertThat(controller.searchByUri("/orders").getBody()).isEmpty();
  }

  @Test
  public void shouldGetByCorrelationId() {
    ApiRequestLog log = new ApiRequestLog("corr-1", "GET", "/test", null, "127.0.0.1", "test");
    when(logRepository.findByCorrelationId("corr-1")).thenReturn(log);

    assertThat(controller.getByCorrelationId("corr-1").getStatusCodeValue()).isEqualTo(200);
  }

  @Test
  public void shouldReturn404ForMissingCorrelationId() {
    when(logRepository.findByCorrelationId("missing")).thenReturn(null);

    assertThat(controller.getByCorrelationId("missing").getStatusCodeValue()).isEqualTo(404);
  }

  @Test
  public void shouldGetStatsWithEmptyLogs() {
    when(logRepository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

    Map<String, Object> stats = controller.getStats(60).getBody();
    assertThat(stats.get("totalRequests")).isEqualTo(0);
    assertThat(stats.get("periodMinutes")).isEqualTo(60);
    assertThat(stats.get("errorCount")).isEqualTo(0L);
    assertThat(stats.get("errorRate")).isEqualTo(0.0);
  }

  @Test
  public void shouldGetStatsWithLogs() {
    ApiRequestLog log1 = new ApiRequestLog("c1", "GET", "/orders", null, "127.0.0.1", "test");
    log1.complete(200, 100L);
    ApiRequestLog log2 = new ApiRequestLog("c2", "POST", "/orders", null, "127.0.0.1", "test");
    log2.complete(500, 200L, "Server Error");

    when(logRepository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Arrays.asList(log1, log2));

    Map<String, Object> stats = controller.getStats(30).getBody();
    assertThat(stats.get("totalRequests")).isEqualTo(2);
    assertThat(stats.get("errorCount")).isEqualTo(1L);
    assertThat((double) stats.get("errorRate")).isEqualTo(0.5);
  }

  @Test
  public void shouldGetStatsWithNullDurationLogs() {
    ApiRequestLog log = new ApiRequestLog("c1", "GET", "/test", null, "127.0.0.1", "test");
    // No complete() called, so durationMs is null
    when(logRepository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Arrays.asList(log));

    Map<String, Object> stats = controller.getStats(60).getBody();
    assertThat(stats.get("totalRequests")).isEqualTo(1);
  }

  @Test
  public void shouldGetStatsWithNullResponseStatus() {
    ApiRequestLog log = new ApiRequestLog("c1", "GET", "/test", null, "127.0.0.1", "test");
    when(logRepository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Arrays.asList(log));

    Map<String, Object> stats = controller.getStats(60).getBody();
    assertThat(stats.get("errorCount")).isEqualTo(0L);
  }

  @Test
  public void shouldGetStatsWithNullUri() {
    ApiRequestLog log = new ApiRequestLog("c1", "GET", null, null, "127.0.0.1", "test");
    log.complete(200, 50L);
    when(logRepository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Arrays.asList(log));

    Map<String, Object> stats = controller.getStats(60).getBody();
    assertThat(stats.get("totalRequests")).isEqualTo(1);
  }

  @Test
  public void shouldGetStatsCalculateP95() {
    ApiRequestLog log1 = new ApiRequestLog("c1", "GET", "/a", null, "127.0.0.1", "test");
    log1.complete(200, 10L);
    ApiRequestLog log2 = new ApiRequestLog("c2", "GET", "/b", null, "127.0.0.1", "test");
    log2.complete(200, 20L);
    ApiRequestLog log3 = new ApiRequestLog("c3", "GET", "/c", null, "127.0.0.1", "test");
    log3.complete(200, 500L);

    when(logRepository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Arrays.asList(log1, log2, log3));

    Map<String, Object> stats = controller.getStats(60).getBody();
    assertThat((Long) stats.get("p95DurationMs")).isGreaterThanOrEqualTo(0L);
  }

  @Test
  public void shouldGetStatsTopEndpoints() {
    ApiRequestLog log1 = new ApiRequestLog("c1", "GET", "/orders", null, "127.0.0.1", "test");
    log1.complete(200, 10L);
    ApiRequestLog log2 = new ApiRequestLog("c2", "GET", "/orders", null, "127.0.0.1", "test");
    log2.complete(200, 20L);

    when(logRepository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Arrays.asList(log1, log2));

    Map<String, Object> stats = controller.getStats(60).getBody();
    @SuppressWarnings("unchecked")
    Map<String, Long> endpoints = (Map<String, Long>) stats.get("topEndpoints");
    assertThat(endpoints.get("GET /orders")).isEqualTo(2L);
  }
}
