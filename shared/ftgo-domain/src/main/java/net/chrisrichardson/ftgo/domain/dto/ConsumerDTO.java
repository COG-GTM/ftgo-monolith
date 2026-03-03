package net.chrisrichardson.ftgo.domain.dto;

/**
 * Data Transfer Object for Consumer entity used in cross-service communication.
 * Decouples the JPA entity from the API contract.
 */
public class ConsumerDTO {

    private Long consumerId;
    private String firstName;
    private String lastName;

    public ConsumerDTO() {
    }

    public ConsumerDTO(Long consumerId, String firstName, String lastName) {
        this.consumerId = consumerId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(Long consumerId) {
        this.consumerId = consumerId;
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
}
