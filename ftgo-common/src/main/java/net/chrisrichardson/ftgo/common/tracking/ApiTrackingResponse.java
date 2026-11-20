package net.chrisrichardson.ftgo.common.tracking;

import java.time.LocalDateTime;

public final class ApiTrackingResponse {

  private ApiTrackingResponse() {
  }

  public static final class ApiRequestLog {
    private final Long id;
    private final String correlationId;
    private final String httpMethod;
    private final String requestUri;
    private final String queryString;
    private final Integer responseStatus;
    private final Long durationMs;
    private final String remoteAddr;
    private final String userAgent;
    private final String errorMessage;
    private final LocalDateTime requestTimestamp;

    public ApiRequestLog(net.chrisrichardson.ftgo.common.tracking.ApiRequestLog log) {
      id = log.getId();
      correlationId = log.getCorrelationId();
      httpMethod = log.getHttpMethod();
      requestUri = log.getRequestUri();
      queryString = log.getQueryString();
      responseStatus = log.getResponseStatus();
      durationMs = log.getDurationMs();
      remoteAddr = log.getRemoteAddr();
      userAgent = log.getUserAgent();
      errorMessage = log.getErrorMessage();
      requestTimestamp = log.getRequestTimestamp();
    }

    public Long getId() { return id; }
    public String getCorrelationId() { return correlationId; }
    public String getHttpMethod() { return httpMethod; }
    public String getRequestUri() { return requestUri; }
    public String getQueryString() { return queryString; }
    public Integer getResponseStatus() { return responseStatus; }
    public Long getDurationMs() { return durationMs; }
    public String getRemoteAddr() { return remoteAddr; }
    public String getUserAgent() { return userAgent; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getRequestTimestamp() { return requestTimestamp; }
  }
}
