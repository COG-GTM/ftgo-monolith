package net.chrisrichardson.ftgo.domain;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static net.chrisrichardson.ftgo.domain.DomainOrderFixtures.standardOrder;
import static org.junit.Assert.assertNull;

public class PaymentInformationTest {

  @Test
  public void newPaymentInformationHasNoPaymentToken() {
    PaymentInformation paymentInformation = new PaymentInformation();

    assertNull(ReflectionTestUtils.getField(paymentInformation, "paymentToken"));
  }

  @Test
  public void newOrderHasNoPaymentInformation() {
    assertNull(ReflectionTestUtils.getField(standardOrder(), "paymentInformation"));
  }
}
