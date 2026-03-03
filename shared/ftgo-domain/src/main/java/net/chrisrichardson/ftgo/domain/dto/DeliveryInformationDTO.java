package net.chrisrichardson.ftgo.domain.dto;

/**
 * Data Transfer Object for DeliveryInformation used in cross-service communication.
 * Contains delivery address and timing details.
 */
public class DeliveryInformationDTO {

    private String deliveryTime;
    private String deliveryAddressStreet1;
    private String deliveryAddressStreet2;
    private String deliveryAddressCity;
    private String deliveryAddressState;
    private String deliveryAddressZip;

    public DeliveryInformationDTO() {
    }

    public DeliveryInformationDTO(String deliveryTime, String deliveryAddressStreet1,
                                  String deliveryAddressStreet2, String deliveryAddressCity,
                                  String deliveryAddressState, String deliveryAddressZip) {
        this.deliveryTime = deliveryTime;
        this.deliveryAddressStreet1 = deliveryAddressStreet1;
        this.deliveryAddressStreet2 = deliveryAddressStreet2;
        this.deliveryAddressCity = deliveryAddressCity;
        this.deliveryAddressState = deliveryAddressState;
        this.deliveryAddressZip = deliveryAddressZip;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getDeliveryAddressStreet1() {
        return deliveryAddressStreet1;
    }

    public void setDeliveryAddressStreet1(String deliveryAddressStreet1) {
        this.deliveryAddressStreet1 = deliveryAddressStreet1;
    }

    public String getDeliveryAddressStreet2() {
        return deliveryAddressStreet2;
    }

    public void setDeliveryAddressStreet2(String deliveryAddressStreet2) {
        this.deliveryAddressStreet2 = deliveryAddressStreet2;
    }

    public String getDeliveryAddressCity() {
        return deliveryAddressCity;
    }

    public void setDeliveryAddressCity(String deliveryAddressCity) {
        this.deliveryAddressCity = deliveryAddressCity;
    }

    public String getDeliveryAddressState() {
        return deliveryAddressState;
    }

    public void setDeliveryAddressState(String deliveryAddressState) {
        this.deliveryAddressState = deliveryAddressState;
    }

    public String getDeliveryAddressZip() {
        return deliveryAddressZip;
    }

    public void setDeliveryAddressZip(String deliveryAddressZip) {
        this.deliveryAddressZip = deliveryAddressZip;
    }
}
