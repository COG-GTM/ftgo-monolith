package com.ftgo.domain.dto;

import java.util.List;

/**
 * Courier summary DTO for cross-service communication.
 * Replaces direct Courier entity sharing between services.
 */
public class CourierDetailsDto {

    private Long courierId;
    private String firstName;
    private String lastName;
    private boolean available;
    private List<DeliveryActionDto> actions;

    public CourierDetailsDto() {
    }

    public CourierDetailsDto(Long courierId, String firstName, String lastName,
                             boolean available, List<DeliveryActionDto> actions) {
        this.courierId = courierId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.available = available;
        this.actions = actions;
    }

    public Long getCourierId() { return courierId; }
    public void setCourierId(Long courierId) { this.courierId = courierId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public List<DeliveryActionDto> getActions() { return actions; }
    public void setActions(List<DeliveryActionDto> actions) { this.actions = actions; }
}
