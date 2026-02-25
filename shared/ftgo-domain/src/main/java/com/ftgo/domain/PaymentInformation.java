package com.ftgo.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;

@Access(AccessType.FIELD)
public class PaymentInformation {

  private String paymentToken;
}
