package net.chrisrichardson.ftgo.courierservice.domain;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Embeddable
public class CourierAction {

    @Enumerated(EnumType.STRING)
    private ActionType type;

    private LocalDateTime time;

    private Long orderId;

    public CourierAction() {
    }

    public CourierAction(ActionType type, Long orderId, LocalDateTime time) {
        this.type = type;
        this.orderId = orderId;
        this.time = time;
    }

    public static CourierAction makePickup(Long orderId) {
        return new CourierAction(ActionType.PICKUP, orderId, null);
    }

    public static CourierAction makeDropoff(Long orderId, LocalDateTime deliveryTime) {
        return new CourierAction(ActionType.DROPOFF, orderId, deliveryTime);
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
