package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/tracking")
public class ApiTrackingController {

  private final ApiRequestLogRepository apiRequestLogRepository;
  private final HandlerExceptionResolver handlerExceptionResolver;

  public ApiTrackingController(ApiRequestLogRepository apiRequestLogRepository,
          @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
    this.apiRequestLogRepository = apiRequestLogRepository;
    this.handlerExceptionResolver = handlerExceptionResolver;
  }

  @ExceptionHandler({TypeMismatchException.class, MissingServletRequestParameterException.class})
  public ModelAndView handleParameterException(Exception exception, HttpServletRequest request,
          HttpServletResponse response) throws Exception {
    String parameter = exception instanceof MissingServletRequestParameterException
            ? ((MissingServletRequestParameterException) exception).getParameterName()
            : exception instanceof MethodArgumentTypeMismatchException
                    ? ((MethodArgumentTypeMismatchException) exception).getName() : null;
    if (!"minutesBack".equals(parameter) && !"uri".equals(parameter)) {
      throw exception;
    }
    String message = "minutesBack".equals(parameter)
            ? "minutesBack must be an integer between 1 and 10080" : "uri is required";
    return handlerExceptionResolver.resolveException(request, response, null,
            new IllegalArgumentException(message));
  }

  private void validateMinutesBack(int minutesBack) {
    if (minutesBack < 1 || minutesBack > 10080) {
      throw new IllegalArgumentException("minutesBack must be an integer between 1 and 10080");
    }
  }

  private List<ApiTrackingResponse.ApiRequestLog> toResponses(List<ApiRequestLog> logs) {
    return logs.stream().map(ApiTrackingResponse.ApiRequestLog::new).collect(Collectors.toList());
  }

  @RequestMapping(path = "/logs", method = RequestMethod.GET)
  public ResponseEntity<List<ApiTrackingResponse.ApiRequestLog>> getRecentLogs(
          @RequestParam(defaultValue = "60") int minutesBack) {
    validateMinutesBack(minutesBack);
    LocalDateTime since = LocalDateTime.now().minusMinutes(minutesBack);
    List<ApiRequestLog> logs = apiRequestLogRepository.findRecentLogs(since);
    return new ResponseEntity<>(toResponses(logs), HttpStatus.OK);
  }

  @RequestMapping(path = "/logs/errors", method = RequestMethod.GET)
  public ResponseEntity<List<ApiTrackingResponse.ApiRequestLog>> getErrors(
          @RequestParam(defaultValue = "60") int minutesBack) {
    validateMinutesBack(minutesBack);
    LocalDateTime since = LocalDateTime.now().minusMinutes(minutesBack);
    List<ApiRequestLog> logs = apiRequestLogRepository.findErrorsSince(since);
    return new ResponseEntity<>(toResponses(logs), HttpStatus.OK);
  }

  @RequestMapping(path = "/logs/search", method = RequestMethod.GET)
  public ResponseEntity<List<ApiTrackingResponse.ApiRequestLog>> searchByUri(@RequestParam String uri) {
    List<ApiRequestLog> logs = apiRequestLogRepository.findByRequestUri(uri);
    return new ResponseEntity<>(toResponses(logs), HttpStatus.OK);
  }

  @RequestMapping(path = "/logs/{correlationId}", method = RequestMethod.GET)
  public ResponseEntity<ApiTrackingResponse.ApiRequestLog> getByCorrelationId(@PathVariable String correlationId) {
    ApiRequestLog log = apiRequestLogRepository.findByCorrelationId(correlationId);
    if (log == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(new ApiTrackingResponse.ApiRequestLog(log), HttpStatus.OK);
  }

  @RequestMapping(path = "/stats", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getStats(
          @RequestParam(defaultValue = "60") int minutesBack) {
    validateMinutesBack(minutesBack);
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

    long[] durations = logs.stream()
            .filter(l -> l.getDurationMs() != null)
            .mapToLong(ApiRequestLog::getDurationMs)
            .sorted()
            .toArray();
    long p95Duration = durations.length == 0 ? 0 : durations[(int) (durations.length * 0.95)];
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
