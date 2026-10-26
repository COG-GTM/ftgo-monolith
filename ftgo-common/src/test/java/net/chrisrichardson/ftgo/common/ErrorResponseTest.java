package net.chrisrichardson.ftgo.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

  @Test
  void shouldCreateErrorResponseWithAllFields() {
    ErrorResponse response = new ErrorResponse(404, "Not Found", "Order not found", "/orders/1", "abc-123");

    assertThat(response.getStatus()).isEqualTo(404);
    assertThat(response.getError()).isEqualTo("Not Found");
    assertThat(response.getMessage()).isEqualTo("Order not found");
    assertThat(response.getPath()).isEqualTo("/orders/1");
    assertThat(response.getCorrelationId()).isEqualTo("abc-123");
    assertThat(response.getTimestamp()).isNotNull();
  }

  @Test
  void shouldCreateDefaultErrorResponse() {
    ErrorResponse response = new ErrorResponse();

    assertThat(response.getStatus()).isEqualTo(0);
    assertThat(response.getError()).isNull();
    assertThat(response.getMessage()).isNull();
    assertThat(response.getPath()).isNull();
    assertThat(response.getCorrelationId()).isNull();
    assertThat(response.getTimestamp()).isNull();
  }
}
