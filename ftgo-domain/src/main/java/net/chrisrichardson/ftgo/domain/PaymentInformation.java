package net.chrisrichardson.ftgo.domain;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Embeddable;

// Value object embedded in Order; maps to the payment_token column of the orders table.
@Embeddable
@Access(AccessType.FIELD)
public class PaymentInformation {

  private String paymentToken;
}
