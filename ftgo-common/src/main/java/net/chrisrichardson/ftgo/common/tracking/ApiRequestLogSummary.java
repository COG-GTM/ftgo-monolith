package net.chrisrichardson.ftgo.common.tracking;

import java.time.LocalDateTime;

/**
 * Redacted view of an {@link ApiRequestLog}. Query strings, user agents and raw
 * exception messages are never exposed and client IP addresses are masked.
 */
public class ApiRequestLogSummary {

  private final Long id;
  private final String correlationId;
  private final String httpMethod;
  private final String requestUri;
  private final Integer responseStatus;
  private final Long durationMs;
  private final String clientAddress;
  private final boolean failed;
  private final LocalDateTime requestTimestamp;

  private ApiRequestLogSummary(Long id, String correlationId, String httpMethod, String requestUri,
                               Integer responseStatus, Long durationMs, String clientAddress,
                               boolean failed, LocalDateTime requestTimestamp) {
    this.id = id;
    this.correlationId = correlationId;
    this.httpMethod = httpMethod;
    this.requestUri = requestUri;
    this.responseStatus = responseStatus;
    this.durationMs = durationMs;
    this.clientAddress = clientAddress;
    this.failed = failed;
    this.requestTimestamp = requestTimestamp;
  }

  public static ApiRequestLogSummary of(ApiRequestLog log) {
    boolean failed = (log.getResponseStatus() != null && log.getResponseStatus() >= 400)
            || log.getErrorMessage() != null;
    return new ApiRequestLogSummary(log.getId(), log.getCorrelationId(), log.getHttpMethod(),
            log.getRequestUri(), log.getResponseStatus(), log.getDurationMs(),
            maskAddress(log.getRemoteAddr()), failed, log.getRequestTimestamp());
  }

  static String maskAddress(String remoteAddr) {
    if (remoteAddr == null || remoteAddr.isEmpty()) {
      return null;
    }
    if (remoteAddr.indexOf(':') >= 0) {
      String[] groups = remoteAddr.split(":", -1);
      StringBuilder masked = new StringBuilder();
      for (int i = 0; i < groups.length; i++) {
        masked.append(i < 2 ? groups[i] : "x");
        if (i < groups.length - 1) {
          masked.append(':');
        }
      }
      return masked.toString();
    }
    String[] octets = remoteAddr.split("\\.", -1);
    if (octets.length != 4) {
      return "x";
    }
    return octets[0] + "." + octets[1] + ".x.x";
  }

  public Long getId() {
    return id;
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

  public String getClientAddress() {
    return clientAddress;
  }

  public boolean isFailed() {
    return failed;
  }

  public LocalDateTime getRequestTimestamp() {
    return requestTimestamp;
  }
}
