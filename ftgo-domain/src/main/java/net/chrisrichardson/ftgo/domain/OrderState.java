package net.chrisrichardson.ftgo.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static java.util.stream.Collectors.toList;

public enum OrderState {
  APPROVED,
  ACCEPTED, PREPARING, READY_FOR_PICKUP, PICKED_UP, DELIVERED,
  CANCELLED,
  ;

  private static final EnumSet<OrderState> TERMINAL_STATES = EnumSet.of(DELIVERED, CANCELLED);

  private static final List<OrderState> NON_TERMINAL_STATES =
          Collections.unmodifiableList(Arrays.stream(values()).filter(s -> !s.isTerminal()).collect(toList()));

  public boolean isTerminal() {
    return TERMINAL_STATES.contains(this);
  }

  /**
   * The states an order can sit in while still awaiting work, in lifecycle order.
   */
  public static List<OrderState> nonTerminalStates() {
    return NON_TERMINAL_STATES;
  }
}
