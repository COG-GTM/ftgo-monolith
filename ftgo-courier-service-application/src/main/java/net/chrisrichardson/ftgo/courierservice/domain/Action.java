package net.chrisrichardson.ftgo.courierservice.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

/**
 * Courier-service copy of the courier delivery {@code Action}.
 *
 * <p>Unlike the shared {@code net.chrisrichardson.ftgo.domain.Action}, this copy does NOT hold a
 * JPA {@code @ManyToOne Order} reference. The extracted courier service owns no {@code orders}
 * table, so the link to an order is stored as a plain {@code order_id} column. The Order/Action
 * coupling stays in {@code ftgo-domain} for the in-process assignment-strategy logic.
 */
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
