package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_state")
    private OrderState orderState;

    private Long consumerId;
    private Long restaurantId;
    private Long assignedCourierId;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "order_minimum"))
    private Money orderMinimum;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street1", column = @Column(name = "delivery_address_street1")),
            @AttributeOverride(name = "street2", column = @Column(name = "delivery_address_street2")),
            @AttributeOverride(name = "city", column = @Column(name = "delivery_address_city")),
            @AttributeOverride(name = "state", column = @Column(name = "delivery_address_state")),
            @AttributeOverride(name = "zip", column = @Column(name = "delivery_address_zip"))
    })
    private Address deliveryAddress;

    private String paymentToken;
    private LocalDateTime acceptTime;
    private LocalDateTime preparingTime;
    private LocalDateTime readyForPickupTime;
    private LocalDateTime pickedUpTime;
    private LocalDateTime deliveredTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime readyBy;
    private Integer previousTicketState;

    @Version
    private Long version;

    protected Order() {
    }

    public Order(Long consumerId, Long restaurantId, Address deliveryAddress) {
        this.consumerId = consumerId;
        this.restaurantId = restaurantId;
        this.deliveryAddress = deliveryAddress;
        this.orderState = OrderState.APPROVAL_PENDING;
    }

    public Long getId() {
        return id;
    }

    public OrderState getOrderState() {
        return orderState;
    }

    public Long getConsumerId() {
        return consumerId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public Long getAssignedCourierId() {
        return assignedCourierId;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public Money getOrderMinimum() {
        return orderMinimum;
    }
}
