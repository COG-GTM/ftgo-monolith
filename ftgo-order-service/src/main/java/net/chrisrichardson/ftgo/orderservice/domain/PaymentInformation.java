package net.chrisrichardson.ftgo.orderservice.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;

@Embeddable
@Access(AccessType.FIELD)
public class PaymentInformation {

  private String paymentToken;
}
