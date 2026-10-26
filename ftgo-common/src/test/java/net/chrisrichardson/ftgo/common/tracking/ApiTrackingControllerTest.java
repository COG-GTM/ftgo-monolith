package net.chrisrichardson.ftgo.common.tracking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiTrackingControllerTest {

  @Mock
  private ApiRequestLogRepository apiRequestLogRepository;

  private ApiTrackingController controller;

  @BeforeEach
  void setUp() {
    controller = new ApiTrackingController(apiRequestLogRepository);
  }

  @Test
  void shouldGetRecentLogs() {
    ApiRequestLog log = new ApiRequestLog("corr-1", "GET", "/orders", null, "127.0.0.1", null);
    when(apiRequestLogRepository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Collections.singletonList(log));

    ResponseEntity<List<ApiRequestLog>> response = controller.getRecentLogs(60);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
  }

  @Test
  void shouldGetErrors() {
    when(apiRequestLogRepository.findErrorsSince(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

    ResponseEntity<List<ApiRequestLog>> response = controller.getErrors(30);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  void shouldSearchByUri() {
    when(apiRequestLogRepository.findByRequestUri("/orders")).thenReturn(Collections.emptyList());

    ResponseEntity<List<ApiRequestLog>> response = controller.searchByUri("/orders");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldGetByCorrelationId() {
    ApiRequestLog log = new ApiRequestLog("corr-1", "GET", "/orders", null, "127.0.0.1", null);
    when(apiRequestLogRepository.findByCorrelationId("corr-1")).thenReturn(log);

    ResponseEntity<ApiRequestLog> response = controller.getByCorrelationId("corr-1");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCorrelationId()).isEqualTo("corr-1");
  }

  @Test
  void shouldReturn404WhenCorrelationIdNotFound() {
    when(apiRequestLogRepository.findByCorrelationId("nonexistent")).thenReturn(null);

    ResponseEntity<ApiRequestLog> response = controller.getByCorrelationId("nonexistent");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldGetStatsWithLogs() {
    ApiRequestLog log1 = new ApiRequestLog("c1", "GET", "/orders", null, "127.0.0.1", null);
    log1.complete(200, 50L);
    ApiRequestLog log2 = new ApiRequestLog("c2", "POST", "/orders", null, "127.0.0.1", null);
    log2.complete(500, 120L, "Error");
    when(apiRequestLogRepository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Arrays.asList(log1, log2));

    ResponseEntity<Map<String, Object>> response = controller.getStats(60);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Object> stats = response.getBody();
    assertThat(stats).isNotNull();
    assertThat(stats.get("totalRequests")).isEqualTo(2);
    assertThat(stats.get("errorCount")).isEqualTo(1L);
    assertThat(stats.get("periodMinutes")).isEqualTo(60);
  }

  @Test
  void shouldGetStatsWithEmptyLogs() {
    when(apiRequestLogRepository.findRecentLogs(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

    ResponseEntity<Map<String, Object>> response = controller.getStats(60);

    Map<String, Object> stats = response.getBody();
    assertThat(stats).isNotNull();
    assertThat(stats.get("totalRequests")).isEqualTo(0);
    assertThat(stats.get("errorRate")).isEqualTo(0.0);
  }

  @Test
  void shouldCalculateP95Duration() {
    ApiRequestLog log1 = new ApiRequestLog("c1", "GET", "/a", null, "127.0.0.1", null);
    log1.complete(200, 10L);
    ApiRequestLog log2 = new ApiRequestLog("c2", "GET", "/b", null, "127.0.0.1", null);
    log2.complete(200, 100L);
    when(apiRequestLogRepository.findRecentLogs(any())).thenReturn(Arrays.asList(log1, log2));

    ResponseEntity<Map<String, Object>> response = controller.getStats(60);

    assertThat(response.getBody()).containsKey("p95DurationMs");
  }

  @Test
  void shouldCalculateStatusCodeDistribution() {
    ApiRequestLog log1 = new ApiRequestLog("c1", "GET", "/a", null, "127.0.0.1", null);
    log1.complete(200, 10L);
    ApiRequestLog log2 = new ApiRequestLog("c2", "GET", "/b", null, "127.0.0.1", null);
    log2.complete(404, 20L);
    when(apiRequestLogRepository.findRecentLogs(any())).thenReturn(Arrays.asList(log1, log2));

    ResponseEntity<Map<String, Object>> response = controller.getStats(60);

    @SuppressWarnings("unchecked")
    Map<String, Long> statusDist = (Map<String, Long>) response.getBody().get("statusCodeDistribution");
    assertThat(statusDist).containsEntry("200", 1L).containsEntry("404", 1L);
  }

  @Test
  void shouldHandleLogsWithNullDuration() {
    ApiRequestLog log1 = new ApiRequestLog("c1", "GET", "/a", null, "127.0.0.1", null);
    when(apiRequestLogRepository.findRecentLogs(any())).thenReturn(Collections.singletonList(log1));

    ResponseEntity<Map<String, Object>> response = controller.getStats(60);

    assertThat(response.getBody().get("avgDurationMs")).isEqualTo(0.0);
  }

  @Test
  void shouldHandleLogsWithNullResponseStatus() {
    ApiRequestLog log1 = new ApiRequestLog("c1", "GET", "/a", null, "127.0.0.1", null);
    when(apiRequestLogRepository.findRecentLogs(any())).thenReturn(Collections.singletonList(log1));

    ResponseEntity<Map<String, Object>> response = controller.getStats(60);

    assertThat(response.getBody().get("errorCount")).isEqualTo(0L);
  }
}
