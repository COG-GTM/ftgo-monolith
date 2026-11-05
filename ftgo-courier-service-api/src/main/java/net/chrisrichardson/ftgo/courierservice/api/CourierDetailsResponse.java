package net.chrisrichardson.ftgo.courierservice.api;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;

import java.time.LocalDateTime;

public class CourierDetailsResponse {

  private long courierId;
  private PersonName name;
  private Address address;
  private boolean available;
  private int activeDeliveryCount;
  private Double currentLatitude;
  private Double currentLongitude;
  private LocalDateTime lastLocationUpdate;
  private CourierPlanResponse plan;

  public CourierDetailsResponse() {
  }

  public CourierDetailsResponse(long courierId, PersonName name, Address address, boolean available,
                                int activeDeliveryCount, Double currentLatitude, Double currentLongitude,
                                LocalDateTime lastLocationUpdate, CourierPlanResponse plan) {
    this.courierId = courierId;
    this.name = name;
    this.address = address;
    this.available = available;
    this.activeDeliveryCount = activeDeliveryCount;
    this.currentLatitude = currentLatitude;
    this.currentLongitude = currentLongitude;
    this.lastLocationUpdate = lastLocationUpdate;
    this.plan = plan;
  }

  public long getCourierId() {
    return courierId;
  }

  public void setCourierId(long courierId) {
    this.courierId = courierId;
  }

  public PersonName getName() {
    return name;
  }

  public void setName(PersonName name) {
    this.name = name;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }

  public int getActiveDeliveryCount() {
    return activeDeliveryCount;
  }

  public void setActiveDeliveryCount(int activeDeliveryCount) {
    this.activeDeliveryCount = activeDeliveryCount;
  }

  public Double getCurrentLatitude() {
    return currentLatitude;
  }

  public void setCurrentLatitude(Double currentLatitude) {
    this.currentLatitude = currentLatitude;
  }

  public Double getCurrentLongitude() {
    return currentLongitude;
  }

  public void setCurrentLongitude(Double currentLongitude) {
    this.currentLongitude = currentLongitude;
  }

  public LocalDateTime getLastLocationUpdate() {
    return lastLocationUpdate;
  }

  public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) {
    this.lastLocationUpdate = lastLocationUpdate;
  }

  public CourierPlanResponse getPlan() {
    return plan;
  }

  public void setPlan(CourierPlanResponse plan) {
    this.plan = plan;
  }
}
