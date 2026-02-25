package com.ftgo.domain.dto;

/**
 * Consumer summary DTO for cross-service communication.
 * Replaces direct Consumer entity sharing between services.
 */
public class ConsumerDetailsDto {

    private Long consumerId;
    private String firstName;
    private String lastName;

    public ConsumerDetailsDto() {
    }

    public ConsumerDetailsDto(Long consumerId, String firstName, String lastName) {
        this.consumerId = consumerId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Long getConsumerId() { return consumerId; }
    public void setConsumerId(Long consumerId) { this.consumerId = consumerId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
