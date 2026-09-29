package net.chrisrichardson.ftgo.common.tracking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/tracking")
@Tag(name = "API Tracking")
public class ApiTrackingController {

  private final ApiRequestLogRepository apiRequestLogRepository;

  public ApiTrackingController(ApiRequestLogRepository apiRequestLogRepository) {
    this.apiRequestLogRepository = apiRequestLogRepository;
  }

  @Operation(summary = "List recent API requests",
          responses = @ApiResponse(responseCode = "200", description = "Requests logged within the period, newest first",
                  content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiRequestLog.class)),
                          examples = @ExampleObject(name = "Recent requests", value = ApiTrackingExamples.LOGS_RESPONSE))))
  @RequestMapping(path = "/logs", method = RequestMethod.GET)
  public ResponseEntity<List<ApiRequestLog>> getRecentLogs(
          @Parameter(description = "How far back to look, in minutes", example = "60") @RequestParam(defaultValue = "60") int minutesBack) {
    LocalDateTime since = LocalDateTime.now().minusMinutes(minutesBack);
    List<ApiRequestLog> logs = apiRequestLogRepository.findRecentLogs(since);
    return new ResponseEntity<>(logs, HttpStatus.OK);
  }

  @Operation(summary = "List recent failed API requests",
          description = "Requests whose response status was 400 or higher.",
          responses = @ApiResponse(responseCode = "200", description = "Failed requests within the period",
                  content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiRequestLog.class)),
                          examples = @ExampleObject(name = "Failed requests", value = ApiTrackingExamples.ERROR_LOGS_RESPONSE))))
  @RequestMapping(path = "/logs/errors", method = RequestMethod.GET)
  public ResponseEntity<List<ApiRequestLog>> getErrors(
          @Parameter(description = "How far back to look, in minutes", example = "60") @RequestParam(defaultValue = "60") int minutesBack) {
    LocalDateTime since = LocalDateTime.now().minusMinutes(minutesBack);
    List<ApiRequestLog> logs = apiRequestLogRepository.findErrorsSince(since);
    return new ResponseEntity<>(logs, HttpStatus.OK);
  }

  @Operation(summary = "Find API requests by URI",
          responses = @ApiResponse(responseCode = "200", description = "Requests whose URI contains the given value, newest first",
                  content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiRequestLog.class)),
                          examples = @ExampleObject(name = "Matching requests", value = ApiTrackingExamples.LOGS_RESPONSE))))
  @RequestMapping(path = "/logs/search", method = RequestMethod.GET)
  public ResponseEntity<List<ApiRequestLog>> searchByUri(
          @Parameter(description = "Substring of the request URI to match", example = "/orders/1") @RequestParam String uri) {
    List<ApiRequestLog> logs = apiRequestLogRepository.findByRequestUri(uri);
    return new ResponseEntity<>(logs, HttpStatus.OK);
  }

  @Operation(summary = "Get an API request by correlation id",
          description = "The correlation id is returned to clients in the `X-Correlation-ID` response header.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Request found",
                          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiRequestLog.class),
                                  examples = @ExampleObject(name = "Request", value = ApiTrackingExamples.LOG_RESPONSE))),
                  @ApiResponse(responseCode = "404", description = "No request with that correlation id (empty body)", content = @Content)
          })
  @RequestMapping(path = "/logs/{correlationId}", method = RequestMethod.GET)
  public ResponseEntity<ApiRequestLog> getByCorrelationId(
          @Parameter(description = "Correlation id", example = "3f0c2a8e-6b1d-4d2e-9a57-1c9e7b4f2d10") @PathVariable String correlationId) {
    ApiRequestLog log = apiRequestLogRepository.findByCorrelationId(correlationId);
    if (log == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(log, HttpStatus.OK);
  }

  @Operation(summary = "Get aggregate API request statistics",
          responses = @ApiResponse(responseCode = "200", description = "Request counts, error rate, latency and per-endpoint breakdown",
                  content = @Content(mediaType = "application/json", schema = @Schema(type = "object"),
                          examples = @ExampleObject(name = "Stats", value = ApiTrackingExamples.STATS_RESPONSE))))
  @RequestMapping(path = "/stats", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getStats(
          @Parameter(description = "How far back to look, in minutes", example = "60") @RequestParam(defaultValue = "60") int minutesBack) {
    LocalDateTime since = LocalDateTime.now().minusMinutes(minutesBack);
    List<ApiRequestLog> logs = apiRequestLogRepository.findRecentLogs(since);

    Map<String, Object> stats = new HashMap<>();
    stats.put("totalRequests", logs.size());
    stats.put("periodMinutes", minutesBack);

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
}
