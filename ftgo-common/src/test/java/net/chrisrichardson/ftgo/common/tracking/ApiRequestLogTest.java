package net.chrisrichardson.ftgo.common.tracking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiRequestLogTest {

  @Test
  void shouldCreateLogEntry() {
    ApiRequestLog log = new ApiRequestLog("corr-123", "GET", "/orders/1", "q=test", "127.0.0.1", "Mozilla/5.0");

    assertThat(log.getCorrelationId()).isEqualTo("corr-123");
    assertThat(log.getHttpMethod()).isEqualTo("GET");
    assertThat(log.getRequestUri()).isEqualTo("/orders/1");
    assertThat(log.getQueryString()).isEqualTo("q=test");
    assertThat(log.getRemoteAddr()).isEqualTo("127.0.0.1");
    assertThat(log.getUserAgent()).isEqualTo("Mozilla/5.0");
    assertThat(log.getRequestTimestamp()).isNotNull();
    assertThat(log.getResponseStatus()).isNull();
    assertThat(log.getDurationMs()).isNull();
    assertThat(log.getErrorMessage()).isNull();
  }

  @Test
  void shouldCompleteWithoutError() {
    ApiRequestLog log = new ApiRequestLog("corr-123", "GET", "/orders", null, "127.0.0.1", null);
    log.complete(200, 50L);

    assertThat(log.getResponseStatus()).isEqualTo(200);
    assertThat(log.getDurationMs()).isEqualTo(50L);
    assertThat(log.getErrorMessage()).isNull();
  }

  @Test
  void shouldCompleteWithError() {
    ApiRequestLog log = new ApiRequestLog("corr-456", "POST", "/orders", null, "127.0.0.1", null);
    log.complete(500, 120L, "Internal error");

    assertThat(log.getResponseStatus()).isEqualTo(500);
    assertThat(log.getDurationMs()).isEqualTo(120L);
    assertThat(log.getErrorMessage()).isEqualTo("Internal error");
  }

  @Test
  void shouldCreateDefaultLogEntry() {
    ApiRequestLog log = new ApiRequestLog();

    assertThat(log.getId()).isNull();
    assertThat(log.getCorrelationId()).isNull();
    assertThat(log.getHttpMethod()).isNull();
  }
}
