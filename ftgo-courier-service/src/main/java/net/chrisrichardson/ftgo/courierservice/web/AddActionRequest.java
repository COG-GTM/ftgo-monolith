package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.domain.ActionType;

import java.time.LocalDateTime;

public class AddActionRequest {

    private Long orderId;
    private ActionType actionType;
    private LocalDateTime time;

    public AddActionRequest() {
    }

    public AddActionRequest(Long orderId, ActionType actionType, LocalDateTime time) {
        this.orderId = orderId;
        this.actionType = actionType;
        this.time = time;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
