package net.chrisrichardson.ftgo.common.tracking;

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

  private static final int MAX_MINUTES_BACK = 60 * 24 * 7;

  private final ApiRequestLogRepository apiRequestLogRepository;

  public ApiTrackingController(ApiRequestLogRepository apiRequestLogRepository) {
    this.apiRequestLogRepository = apiRequestLogRepository;
  }

  @RequestMapping(path = "/logs", method = RequestMethod.GET)
  public ResponseEntity<List<ApiRequestLogSummary>> getRecentLogs(
          @RequestParam(defaultValue = "60") int minutesBack) {
    LocalDateTime since = since(minutesBack);
    List<ApiRequestLog> logs = apiRequestLogRepository.findRecentLogs(since);
    return new ResponseEntity<>(toSummaries(logs), HttpStatus.OK);
  }

  @RequestMapping(path = "/logs/errors", method = RequestMethod.GET)
  public ResponseEntity<List<ApiRequestLogSummary>> getErrors(
          @RequestParam(defaultValue = "60") int minutesBack) {
    LocalDateTime since = since(minutesBack);
    List<ApiRequestLog> logs = apiRequestLogRepository.findErrorsSince(since);
    return new ResponseEntity<>(toSummaries(logs), HttpStatus.OK);
  }

  @RequestMapping(path = "/logs/search", method = RequestMethod.GET)
  public ResponseEntity<List<ApiRequestLogSummary>> searchByUri(@RequestParam String uri) {
    List<ApiRequestLog> logs = apiRequestLogRepository.findByRequestUri(uri);
    return new ResponseEntity<>(toSummaries(logs), HttpStatus.OK);
  }

  @RequestMapping(path = "/logs/{correlationId}", method = RequestMethod.GET)
  public ResponseEntity<ApiRequestLogSummary> getByCorrelationId(@PathVariable String correlationId) {
    ApiRequestLog log = apiRequestLogRepository.findByCorrelationId(correlationId);
    if (log == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(ApiRequestLogSummary.of(log), HttpStatus.OK);
  }

  @RequestMapping(path = "/stats", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getStats(
          @RequestParam(defaultValue = "60") int minutesBack) {
    int period = clampMinutesBack(minutesBack);
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

  private static LocalDateTime since(int minutesBack) {
    return LocalDateTime.now().minusMinutes(clampMinutesBack(minutesBack));
  }

  private static int clampMinutesBack(int minutesBack) {
    if (minutesBack < 1) {
      return 1;
    }
    return Math.min(minutesBack, MAX_MINUTES_BACK);
  }

  private static List<ApiRequestLogSummary> toSummaries(List<ApiRequestLog> logs) {
    return logs.stream().map(ApiRequestLogSummary::of).collect(Collectors.toList());
  }
}
