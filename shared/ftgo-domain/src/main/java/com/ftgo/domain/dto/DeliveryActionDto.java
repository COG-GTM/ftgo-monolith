package com.ftgo.domain.dto;

/**
 * Delivery action DTO for cross-service communication.
 * Replaces direct Action embeddable sharing between services.
 */
public class DeliveryActionDto {

    private String actionType;
    private Long orderId;
    private String time;

    public DeliveryActionDto() {
    }

    public DeliveryActionDto(String actionType, Long orderId, String time) {
        this.actionType = actionType;
        this.orderId = orderId;
        this.time = time;
    }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
