package net.chrisrichardson.ftgo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Access(AccessType.FIELD)
@DynamicUpdate
public class Courier {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded
  private PersonName name;

  @Embedded
  private Address address;

  @Embedded
  private Plan plan = new Plan();

  private Boolean available;

  private Double currentLatitude;
  private Double currentLongitude;
  private LocalDateTime lastLocationUpdate;

  @JsonIgnore
  private String accessTokenHash;

  public Courier() {
  }

  public Courier(PersonName name, Address address) {
    this.name = name;
    this.address = address;
    if (address != null && address.getLatitude() != null && address.getLongitude() != null) {
      validateLocation(address.getLatitude(), address.getLongitude());
      this.currentLatitude = address.getLatitude();
      this.currentLongitude = address.getLongitude();
    }
  }

  public void noteAvailable() {
    this.available = true;

  }

  public void addAction(Action action) {
    plan.add(action);
  }

  public void cancelDelivery(Order order) {
    plan.removeDelivery(order);
  }

  public boolean isAvailable() {
    return available != null && available;
  }

  public Plan getPlan() {
    return plan;
  }

  public Long getId() {
    return id;
  }

  public void noteUnavailable() {
    this.available = false;
  }

  public List<Action> actionsForDelivery(Order order) {
    return plan.actionsForDelivery(order);
  }

  public PersonName getName() {
    return name;
  }

  public Address getAddress() {
    return address;
  }

  public Double getCurrentLatitude() {
    return currentLatitude;
  }

  public Double getCurrentLongitude() {
    return currentLongitude;
  }

  public LocalDateTime getLastLocationUpdate() {
    return lastLocationUpdate;
  }

  public void updateLocation(double latitude, double longitude) {
    validateLocation(latitude, longitude);
    this.currentLatitude = latitude;
    this.currentLongitude = longitude;
    this.lastLocationUpdate = LocalDateTime.now();
  }

  public void assignAccessTokenHash(String accessTokenHash) {
    this.accessTokenHash = accessTokenHash;
  }

  public boolean hasAccessTokenHash(String hash) {
    return accessTokenHash != null && accessTokenHash.equals(hash);
  }

  public int getActiveDeliveryCount() {
    if (plan == null || plan.getActions() == null) {
      return 0;
    }
    return (int) plan.getActions().stream()
            .filter(a -> a.getType() == ActionType.PICKUP)
            .count();
  }

  public boolean hasLocation() {
    return currentLatitude != null && currentLongitude != null;
  }

  private static void validateLocation(double latitude, double longitude) {
    if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90 ||
            !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
      throw new IllegalArgumentException("Location coordinates are out of range");
    }
  }
}
