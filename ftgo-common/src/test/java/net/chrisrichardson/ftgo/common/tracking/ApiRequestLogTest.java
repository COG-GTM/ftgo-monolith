package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiRequestLogTest {

  @Test
  public void shouldCreateLogEntry() {
    ApiRequestLog log = new ApiRequestLog("corr-1", "GET", "/orders", "page=1", "127.0.0.1", "Mozilla/5.0");
    assertThat(log.getCorrelationId()).isEqualTo("corr-1");
    assertThat(log.getHttpMethod()).isEqualTo("GET");
    assertThat(log.getRequestUri()).isEqualTo("/orders");
    assertThat(log.getQueryString()).isEqualTo("page=1");
    assertThat(log.getRemoteAddr()).isEqualTo("127.0.0.1");
    assertThat(log.getUserAgent()).isEqualTo("Mozilla/5.0");
    assertThat(log.getRequestTimestamp()).isNotNull();
  }

  @Test
  public void shouldCompleteSuccessfully() {
    ApiRequestLog log = new ApiRequestLog("corr-2", "POST", "/orders", null, "10.0.0.1", "curl");
    log.complete(201, 150L);
    assertThat(log.getResponseStatus()).isEqualTo(201);
    assertThat(log.getDurationMs()).isEqualTo(150L);
    assertThat(log.getErrorMessage()).isNull();
  }

  @Test
  public void shouldCompleteWithError() {
    ApiRequestLog log = new ApiRequestLog("corr-3", "GET", "/orders/999", null, "10.0.0.1", "curl");
    log.complete(500, 42L, "Internal Server Error");
    assertThat(log.getResponseStatus()).isEqualTo(500);
    assertThat(log.getDurationMs()).isEqualTo(42L);
    assertThat(log.getErrorMessage()).isEqualTo("Internal Server Error");
  }

  @Test
  public void shouldCreateDefaultInstance() {
    ApiRequestLog log = new ApiRequestLog();
    assertThat(log.getId()).isNull();
    assertThat(log.getCorrelationId()).isNull();
  }
}
