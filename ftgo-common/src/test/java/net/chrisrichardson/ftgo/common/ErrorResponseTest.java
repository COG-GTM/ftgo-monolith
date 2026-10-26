package net.chrisrichardson.ftgo.common;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorResponseTest {

  @Test
  public void shouldCreateErrorResponseWithAllFields() {
    ErrorResponse response = new ErrorResponse(404, "Not Found", "Order not found", "/orders/99", "abc-123");
    assertThat(response.getStatus()).isEqualTo(404);
    assertThat(response.getError()).isEqualTo("Not Found");
    assertThat(response.getMessage()).isEqualTo("Order not found");
    assertThat(response.getPath()).isEqualTo("/orders/99");
    assertThat(response.getCorrelationId()).isEqualTo("abc-123");
    assertThat(response.getTimestamp()).isNotNull();
  }

  @Test
  public void shouldCreateEmptyErrorResponse() {
    ErrorResponse response = new ErrorResponse();
    assertThat(response.getMessage()).isNull();
    assertThat(response.getStatus()).isEqualTo(0);
    assertThat(response.getError()).isNull();
    assertThat(response.getPath()).isNull();
    assertThat(response.getCorrelationId()).isNull();
    assertThat(response.getTimestamp()).isNull();
  }
}
