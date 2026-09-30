package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/tracking")
public class ApiTrackingController {

  static final int MAX_MINUTES_BACK = 24 * 60;
  static final int DEFAULT_LIMIT = 100;
  static final int MAX_LIMIT = 1000;

  private final ApiRequestLogRepository apiRequestLogRepository;

  public ApiTrackingController(ApiRequestLogRepository apiRequestLogRepository) {
    this.apiRequestLogRepository = apiRequestLogRepository;
  }

  @RequestMapping(path = "/logs", method = RequestMethod.GET)
  public ResponseEntity<List<ApiRequestLogView>> getRecentLogs(
          @RequestParam(defaultValue = "60") int minutesBack,
          @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {
    LocalDateTime since = LocalDateTime.now().minusMinutes(validMinutesBack(minutesBack));
    List<ApiRequestLog> logs = apiRequestLogRepository.findRecentLogs(since, page(limit));
    return new ResponseEntity<>(toViews(logs), HttpStatus.OK);
  }

  @RequestMapping(path = "/logs/errors", method = RequestMethod.GET)
  public ResponseEntity<List<ApiRequestLogView>> getErrors(
          @RequestParam(defaultValue = "60") int minutesBack,
          @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {
    LocalDateTime since = LocalDateTime.now().minusMinutes(validMinutesBack(minutesBack));
    List<ApiRequestLog> logs = apiRequestLogRepository.findErrorsSince(since, page(limit));
    return new ResponseEntity<>(toViews(logs), HttpStatus.OK);
  }

  @RequestMapping(path = "/logs/search", method = RequestMethod.GET)
  public ResponseEntity<List<ApiRequestLogView>> searchByUri(
          @RequestParam String uri,
          @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {
    if (uri.trim().isEmpty()) {
      throw new IllegalArgumentException("uri must not be blank");
    }
    List<ApiRequestLog> logs = apiRequestLogRepository.findByRequestUri(uri.trim(), page(limit));
    return new ResponseEntity<>(toViews(logs), HttpStatus.OK);
  }

  @RequestMapping(path = "/logs/{correlationId}", method = RequestMethod.GET)
  public ResponseEntity<ApiRequestLogView> getByCorrelationId(@PathVariable String correlationId) {
    ApiRequestLog log = apiRequestLogRepository.findByCorrelationId(correlationId);
    if (log == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(new ApiRequestLogView(log), HttpStatus.OK);
  }

  @RequestMapping(path = "/stats", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getStats(
          @RequestParam(defaultValue = "60") int minutesBack) {
    int period = validMinutesBack(minutesBack);
    LocalDateTime since = LocalDateTime.now().minusMinutes(period);
    List<ApiRequestLog> logs = apiRequestLogRepository.findRecentLogs(since);

    Map<String, Object> stats = new HashMap<>();
    stats.put("totalRequests", logs.size());
    stats.put("periodMinutes", period);

    long errorCount = logs.stream()
            .filter(l -> l.getResponseStatus() != null && l.getResponseStatus() >= 400)
            .count();
    stats.put("errorCount", errorCount);
    stats.put("errorRate", logs.isEmpty() ? 0.0 : (double) errorCount / logs.size());

    double avgDuration = logs.stream()
            .filter(l -> l.getDurationMs() != null)
            .mapToLong(ApiRequestLog::getDurationMs)
            .average()
            .orElse(0.0);
    stats.put("avgDurationMs", Math.round(avgDuration * 100.0) / 100.0);

    long p95Duration = logs.stream()
            .filter(l -> l.getDurationMs() != null)
            .mapToLong(ApiRequestLog::getDurationMs)
            .sorted()
            .skip((long) (logs.size() * 0.95))
            .findFirst()
            .orElse(0);
    stats.put("p95DurationMs", p95Duration);

    Map<String, Long> statusCounts = new HashMap<>();
    for (ApiRequestLog log : logs) {
      if (log.getResponseStatus() != null) {
        String key = String.valueOf(log.getResponseStatus());
        statusCounts.merge(key, 1L, Long::sum);
      }
    }
    stats.put("statusCodeDistribution", statusCounts);

    Map<String, Long> endpointCounts = new HashMap<>();
    for (ApiRequestLog log : logs) {
      if (log.getRequestUri() != null) {
        String key = log.getHttpMethod() + " " + log.getRequestUri();
        endpointCounts.merge(key, 1L, Long::sum);
      }
    }
    stats.put("topEndpoints", endpointCounts);

    return new ResponseEntity<>(stats, HttpStatus.OK);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
    Map<String, String> body = new HashMap<>();
    body.put("error", e.getMessage());
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  private static int validMinutesBack(int minutesBack) {
    if (minutesBack < 1 || minutesBack > MAX_MINUTES_BACK) {
      throw new IllegalArgumentException("minutesBack must be between 1 and " + MAX_MINUTES_BACK);
    }
    return minutesBack;
  }

  private static Pageable page(int limit) {
    if (limit < 1 || limit > MAX_LIMIT) {
      throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
    }
    return PageRequest.of(0, limit);
  }

  private static List<ApiRequestLogView> toViews(List<ApiRequestLog> logs) {
    return logs.stream().map(ApiRequestLogView::new).collect(Collectors.toList());
  }
}
