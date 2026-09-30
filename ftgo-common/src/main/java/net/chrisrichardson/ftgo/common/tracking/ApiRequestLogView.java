package net.chrisrichardson.ftgo.common.tracking;

import java.time.LocalDateTime;

/**
 * Operator-facing projection of {@link ApiRequestLog}. Deliberately omits the query string,
 * client address and user agent so that other users' request parameters are never exposed.
 */
public class ApiRequestLogView {

  private final String correlationId;
  private final String httpMethod;
  private final String requestUri;
  private final Integer responseStatus;
  private final Long durationMs;
  private final String errorMessage;
  private final LocalDateTime requestTimestamp;

  public ApiRequestLogView(ApiRequestLog log) {
    this.correlationId = log.getCorrelationId();
    this.httpMethod = log.getHttpMethod();
    this.requestUri = log.getRequestUri();
    this.responseStatus = log.getResponseStatus();
    this.durationMs = log.getDurationMs();
    this.errorMessage = log.getErrorMessage();
    this.requestTimestamp = log.getRequestTimestamp();
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public String getRequestUri() {
    return requestUri;
  }

  public Integer getResponseStatus() {
    return responseStatus;
  }

  public Long getDurationMs() {
    return durationMs;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public LocalDateTime getRequestTimestamp() {
    return requestTimestamp;
  }
}
