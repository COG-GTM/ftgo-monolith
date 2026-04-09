package net.chrisrichardson.ftgo.common.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

public class ApiTrackingInterceptor implements HandlerInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(ApiTrackingInterceptor.class);
  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  private static final String START_TIME_ATTR = "apiTracking.startTime";
  private static final String LOG_ENTRY_ATTR = "apiTracking.logEntry";

  private final ApiRequestLogRepository apiRequestLogRepository;

  public ApiTrackingInterceptor(ApiRequestLogRepository apiRequestLogRepository) {
    this.apiRequestLogRepository = apiRequestLogRepository;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    long startTime = System.currentTimeMillis();
    request.setAttribute(START_TIME_ATTR, startTime);

    String correlationId = request.getHeader(CORRELATION_ID_HEADER);
    if (correlationId == null || correlationId.isEmpty()) {
      correlationId = UUID.randomUUID().toString();
    }

    MDC.put("correlationId", correlationId);
    response.setHeader(CORRELATION_ID_HEADER, correlationId);

    ApiRequestLog logEntry = new ApiRequestLog(
            correlationId,
            request.getMethod(),
            request.getRequestURI(),
            request.getQueryString(),
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
    );

    request.setAttribute(LOG_ENTRY_ATTR, logEntry);

    logger.info("[{}] {} {} started", correlationId, request.getMethod(), request.getRequestURI());

    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
    ApiRequestLog logEntry = (ApiRequestLog) request.getAttribute(LOG_ENTRY_ATTR);

    if (logEntry != null && startTime != null) {
      long durationMs = System.currentTimeMillis() - startTime;

      if (ex != null) {
        logEntry.complete(response.getStatus(), durationMs, ex.getMessage());
      } else {
        logEntry.complete(response.getStatus(), durationMs);
      }

      try {
        apiRequestLogRepository.save(logEntry);
      } catch (Exception saveEx) {
        logger.warn("Failed to persist API request log: {}", saveEx.getMessage());
      }

      String correlationId = logEntry.getCorrelationId();
      logger.info("[{}] {} {} completed: status={} duration={}ms",
              correlationId, request.getMethod(), request.getRequestURI(),
              response.getStatus(), durationMs);
    }

    MDC.remove("correlationId");
  }
}
