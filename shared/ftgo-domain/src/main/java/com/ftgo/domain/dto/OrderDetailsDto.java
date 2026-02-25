package com.ftgo.domain.dto;

import java.util.List;

/**
 * Order summary DTO for cross-service communication.
 * Replaces direct Order entity sharing between services.
 */
public class OrderDetailsDto {

    private Long orderId;
    private String orderState;
    private Long consumerId;
    private Long restaurantId;
    private List<OrderLineItemDto> lineItems;
    private String orderTotal;

    public OrderDetailsDto() {
    }

    public OrderDetailsDto(Long orderId, String orderState, Long consumerId, Long restaurantId,
                           List<OrderLineItemDto> lineItems, String orderTotal) {
        this.orderId = orderId;
        this.orderState = orderState;
        this.consumerId = consumerId;
        this.restaurantId = restaurantId;
        this.lineItems = lineItems;
        this.orderTotal = orderTotal;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderState() { return orderState; }
    public void setOrderState(String orderState) { this.orderState = orderState; }
    public Long getConsumerId() { return consumerId; }
    public void setConsumerId(Long consumerId) { this.consumerId = consumerId; }
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    public List<OrderLineItemDto> getLineItems() { return lineItems; }
    public void setLineItems(List<OrderLineItemDto> lineItems) { this.lineItems = lineItems; }
    public String getOrderTotal() { return orderTotal; }
    public void setOrderTotal(String orderTotal) { this.orderTotal = orderTotal; }
}
