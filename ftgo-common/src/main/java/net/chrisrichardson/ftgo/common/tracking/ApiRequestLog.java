package net.chrisrichardson.ftgo.common.tracking;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_request_log")
public class ApiRequestLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String correlationId;
  private String httpMethod;
  private String requestUri;
  private String queryString;
  private Integer responseStatus;
  private Long durationMs;
  private String remoteAddr;
  private String userAgent;

  @Column(length = 4000)
  private String errorMessage;

  private LocalDateTime requestTimestamp;

  public ApiRequestLog() {
  }

  public ApiRequestLog(String correlationId, String httpMethod, String requestUri,
                       String queryString, String remoteAddr, String userAgent) {
    this.correlationId = correlationId;
    this.httpMethod = httpMethod;
    this.requestUri = requestUri;
    this.queryString = queryString;
    this.remoteAddr = remoteAddr;
    this.userAgent = userAgent;
    this.requestTimestamp = LocalDateTime.now();
  }

  public void complete(int responseStatus, long durationMs) {
    this.responseStatus = responseStatus;
    this.durationMs = durationMs;
  }

  public void complete(int responseStatus, long durationMs, String errorMessage) {
    this.responseStatus = responseStatus;
    this.durationMs = durationMs;
    this.errorMessage = errorMessage;
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

  public String getQueryString() {
    return queryString;
  }

  public Integer getResponseStatus() {
    return responseStatus;
  }

  public Long getDurationMs() {
    return durationMs;
  }

  public String getRemoteAddr() {
    return remoteAddr;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public LocalDateTime getRequestTimestamp() {
    return requestTimestamp;
  }
}
