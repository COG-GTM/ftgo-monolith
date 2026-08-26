package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.domain.OrderState;

public class TerminalOrderStateException extends RuntimeException {

  public TerminalOrderStateException(OrderState orderState) {
    super("Order state " + orderState + " is terminal and has no SLA");
  }
}
