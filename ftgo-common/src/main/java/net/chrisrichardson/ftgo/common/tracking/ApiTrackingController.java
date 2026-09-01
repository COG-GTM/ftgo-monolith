package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/tracking")
public class ApiTrackingController {

  private static final int MAX_MINUTES_BACK = 1440;
  private static final int MAX_RESULTS = 1000;
  private static final int MAX_TOP_ENDPOINTS = 50;

  private final ApiRequestLogRepository apiRequestLogRepository;

  public ApiTrackingController(ApiRequestLogRepository apiRequestLogRepository) {
    this.apiRequestLogRepository = apiRequestLogRepository;
  }

  @RequestMapping(path = "/logs", method = RequestMethod.GET)
  public ResponseEntity<List<ApiRequestLog>> getRecentLogs(
          @RequestParam(defaultValue = "60") int minutesBack) {
    LocalDateTime since = since(minutesBack);
    List<ApiRequestLog> logs = apiRequestLogRepository.findRecentLogs(since, limit());
    return new ResponseEntity<>(logs, HttpStatus.OK);
  }

  @RequestMapping(path = "/logs/errors", method = RequestMethod.GET)
  public ResponseEntity<List<ApiRequestLog>> getErrors(
          @RequestParam(defaultValue = "60") int minutesBack) {
    LocalDateTime since = since(minutesBack);
    List<ApiRequestLog> logs = apiRequestLogRepository.findErrorsSince(since, limit());
    return new ResponseEntity<>(logs, HttpStatus.OK);
  }

  @RequestMapping(path = "/logs/search", method = RequestMethod.GET)
  public ResponseEntity<List<ApiRequestLog>> searchByUri(@RequestParam String uri) {
    List<ApiRequestLog> logs = apiRequestLogRepository.findByRequestUri(uri, limit());
    return new ResponseEntity<>(logs, HttpStatus.OK);
  }

  @RequestMapping(path = "/logs/{correlationId}", method = RequestMethod.GET)
  public ResponseEntity<ApiRequestLog> getByCorrelationId(@PathVariable String correlationId) {
    ApiRequestLog log = apiRequestLogRepository.findByCorrelationId(correlationId);
    if (log == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(log, HttpStatus.OK);
  }

  @Transactional(readOnly = true)
  @RequestMapping(path = "/stats", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getStats(
          @RequestParam(defaultValue = "60") int minutesBack) {
    int period = clampMinutesBack(minutesBack);
    LocalDateTime since = LocalDateTime.now().minusMinutes(period);

    Map<String, Object> stats = new HashMap<>();
    long totalRequests = apiRequestLogRepository.countSince(since);
    stats.put("totalRequests", totalRequests);
    stats.put("periodMinutes", period);

    long errorCount = apiRequestLogRepository.countErrorsSince(since);
    stats.put("errorCount", errorCount);
    stats.put("errorRate", totalRequests == 0 ? 0.0 : (double) errorCount / totalRequests);

    Double avgDuration = apiRequestLogRepository.averageDurationSince(since);
    stats.put("avgDurationMs", avgDuration == null ? 0.0 : Math.round(avgDuration * 100.0) / 100.0);

    stats.put("p95DurationMs", p95DurationMs(since));

    Map<String, Long> statusCounts = new HashMap<>();
    for (Object[] row : apiRequestLogRepository.countByResponseStatusSince(since)) {
      statusCounts.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
    }
    stats.put("statusCodeDistribution", statusCounts);

    Map<String, Long> endpointCounts = new LinkedHashMap<>();
    for (Object[] row : apiRequestLogRepository.countByEndpointSince(since, PageRequest.of(0, MAX_TOP_ENDPOINTS))) {
      endpointCounts.put(row[0] + " " + row[1], ((Number) row[2]).longValue());
    }
    stats.put("topEndpoints", endpointCounts);

    return new ResponseEntity<>(stats, HttpStatus.OK);
  }

  private long p95DurationMs(LocalDateTime since) {
    long timedRequests = apiRequestLogRepository.countDurationsSince(since);
    if (timedRequests == 0) {
      return 0;
    }
    int index = (int) Math.min((long) (timedRequests * 0.95), timedRequests - 1);
    List<Long> durations = apiRequestLogRepository.findDurationsSince(since, PageRequest.of(index, 1));
    return durations.isEmpty() ? 0 : durations.get(0);
  }

  private static int clampMinutesBack(int minutesBack) {
    return Math.max(1, Math.min(minutesBack, MAX_MINUTES_BACK));
  }

  private static LocalDateTime since(int minutesBack) {
    return LocalDateTime.now().minusMinutes(clampMinutesBack(minutesBack));
  }

  private static Pageable limit() {
    return PageRequest.of(0, MAX_RESULTS);
  }
}
