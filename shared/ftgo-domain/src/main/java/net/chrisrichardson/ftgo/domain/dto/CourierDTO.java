package net.chrisrichardson.ftgo.domain.dto;

/**
 * Data Transfer Object for Courier entity used in cross-service communication.
 * Decouples the JPA entity from the API contract.
 */
public class CourierDTO {

    private Long courierId;
    private String firstName;
    private String lastName;
    private boolean available;

    public CourierDTO() {
    }

    public CourierDTO(Long courierId, String firstName, String lastName, boolean available) {
        this.courierId = courierId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.available = available;
    }

    public Long getCourierId() {
        return courierId;
    }

    public void setCourierId(Long courierId) {
        this.courierId = courierId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
