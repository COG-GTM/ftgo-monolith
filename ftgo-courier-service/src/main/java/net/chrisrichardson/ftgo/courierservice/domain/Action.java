package net.chrisrichardson.ftgo.courierservice.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Embeddable
public class Action {

  @Enumerated(EnumType.STRING)
  private ActionType type;
  private LocalDateTime time;

  @Column(name = "order_id")
  private Long orderId;

  private Action() {
  }

  public Action(ActionType type, Long orderId, LocalDateTime time) {
    this.type = type;
    this.orderId = orderId;
    this.time = time;
  }

  public boolean actionFor(Long orderId) {
    return this.orderId != null && this.orderId.equals(orderId);
  }

  public static Action makePickup(Long orderId) {
    return new Action(ActionType.PICKUP, orderId, null);
  }

  public static Action makeDropoff(Long orderId, LocalDateTime deliveryTime) {
    return new Action(ActionType.DROPOFF, orderId, deliveryTime);
  }

  public ActionType getType() {
    return type;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public Long getOrderId() {
    return orderId;
  }
}
