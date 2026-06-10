package net.chrisrichardson.ftgo.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentInformationTest {

  @Test
  void shouldConstruct() {
    PaymentInformation payment = new PaymentInformation();
    assertThat(payment).isNotNull();
  }
}
