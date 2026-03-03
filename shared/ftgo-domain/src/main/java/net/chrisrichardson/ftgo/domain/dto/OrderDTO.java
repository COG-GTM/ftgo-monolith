package net.chrisrichardson.ftgo.domain.dto;

import java.util.List;

/**
 * Data Transfer Object for Order entity used in cross-service communication.
 * Decouples the JPA entity from the API contract so services can communicate
 * without depending on the persistence layer.
 */
public class OrderDTO {

    private Long orderId;
    private Long consumerId;
    private Long restaurantId;
    private String orderState;
    private List<OrderLineItemDTO> lineItems;
    private String orderTotal;

    public OrderDTO() {
    }

    public OrderDTO(Long orderId, Long consumerId, Long restaurantId, String orderState,
                    List<OrderLineItemDTO> lineItems, String orderTotal) {
        this.orderId = orderId;
        this.consumerId = consumerId;
        this.restaurantId = restaurantId;
        this.orderState = orderState;
        this.lineItems = lineItems;
        this.orderTotal = orderTotal;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(Long consumerId) {
        this.consumerId = consumerId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getOrderState() {
        return orderState;
    }

    public void setOrderState(String orderState) {
        this.orderState = orderState;
    }

    public List<OrderLineItemDTO> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<OrderLineItemDTO> lineItems) {
        this.lineItems = lineItems;
    }

    public String getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(String orderTotal) {
        this.orderTotal = orderTotal;
    }
}
