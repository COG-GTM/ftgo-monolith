package net.chrisrichardson.ftgo.domain;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Embeddable
public class Action {

  @Enumerated(EnumType.STRING)
  private ActionType type;
  private LocalDateTime time;

  private Long orderId;

  private Action() {
  }

  public Action(ActionType type, Long orderId, LocalDateTime time) {
    this.type = type;
    this.orderId = orderId;
    this.time = time;
  }

  public boolean actionForOrderId(Long orderId) {
    return this.orderId != null && this.orderId.equals(orderId);
  }

  public static Action makePickupForOrder(Long orderId, LocalDateTime pickupTime) {
    return new Action(ActionType.PICKUP, orderId, pickupTime);
  }

  public static Action makeDropoffForOrder(Long orderId, LocalDateTime deliveryTime) {
    return new Action(ActionType.DROPOFF, orderId, deliveryTime);
  }

  public ActionType getType() {
    return type;
  }

  public Long getOrderId() {
    return orderId;
  }

  public LocalDateTime getTime() {
    return time;
  }

}
