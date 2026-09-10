package net.chrisrichardson.ftgo.common.tracking;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_request_log")
public class ApiRequestLog {

  static final int CORRELATION_ID_MAX_LENGTH = 255;
  static final int HTTP_METHOD_MAX_LENGTH = 10;
  static final int REQUEST_URI_MAX_LENGTH = 1024;
  static final int QUERY_STRING_MAX_LENGTH = 2048;
  static final int REMOTE_ADDR_MAX_LENGTH = 255;
  static final int USER_AGENT_MAX_LENGTH = 1024;
  static final int ERROR_MESSAGE_MAX_LENGTH = 4000;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = CORRELATION_ID_MAX_LENGTH)
  private String correlationId;

  @Column(length = HTTP_METHOD_MAX_LENGTH)
  private String httpMethod;

  @Column(length = REQUEST_URI_MAX_LENGTH)
  private String requestUri;

  @Column(length = QUERY_STRING_MAX_LENGTH)
  private String queryString;

  private Integer responseStatus;
  private Long durationMs;

  @Column(length = REMOTE_ADDR_MAX_LENGTH)
  private String remoteAddr;

  @Column(length = USER_AGENT_MAX_LENGTH)
  private String userAgent;

  @Column(length = ERROR_MESSAGE_MAX_LENGTH)
  private String errorMessage;

  private LocalDateTime requestTimestamp;

  public ApiRequestLog() {
  }

  public ApiRequestLog(String correlationId, String httpMethod, String requestUri,
                       String queryString, String remoteAddr, String userAgent) {
    this.correlationId = truncate(correlationId, CORRELATION_ID_MAX_LENGTH);
    this.httpMethod = truncate(httpMethod, HTTP_METHOD_MAX_LENGTH);
    this.requestUri = truncate(requestUri, REQUEST_URI_MAX_LENGTH);
    this.queryString = truncate(queryString, QUERY_STRING_MAX_LENGTH);
    this.remoteAddr = truncate(remoteAddr, REMOTE_ADDR_MAX_LENGTH);
    this.userAgent = truncate(userAgent, USER_AGENT_MAX_LENGTH);
    this.requestTimestamp = LocalDateTime.now();
  }

  static String truncate(String value, int maxLength) {
    if (value == null || value.length() <= maxLength) {
      return value;
    }
    return value.substring(0, maxLength);
  }

  public void complete(int responseStatus, long durationMs) {
    this.responseStatus = responseStatus;
    this.durationMs = durationMs;
  }

  public void complete(int responseStatus, long durationMs, String errorMessage) {
    this.responseStatus = responseStatus;
    this.durationMs = durationMs;
    this.errorMessage = truncate(errorMessage, ERROR_MESSAGE_MAX_LENGTH);
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
