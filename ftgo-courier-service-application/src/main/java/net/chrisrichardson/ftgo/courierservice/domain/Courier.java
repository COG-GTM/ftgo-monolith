package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Courier-service copy of the {@code Courier} aggregate.
 *
 * <p>This is the entity owned by the extracted courier microservice. It maps to the same
 * {@code courier} / {@code courier_actions} tables but lives in its own database (H2 in dev). It is
 * intentionally decoupled from {@code Order}: the shared {@code net.chrisrichardson.ftgo.domain.Courier}
 * remains in {@code ftgo-domain} for the order-service assignment-strategy logic that still depends
 * on it directly.
 */
@Entity
@Table(name = "courier")
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

  public Courier() {
  }

  public Courier(PersonName name, Address address) {
    this.name = name;
    this.address = address;
    if (address != null && address.getLatitude() != null) {
      this.currentLatitude = address.getLatitude();
      this.currentLongitude = address.getLongitude();
    }
  }

  public void noteAvailable() {
    this.available = true;
  }

  public void noteUnavailable() {
    this.available = false;
  }

  public void addAction(Action action) {
    plan.add(action);
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
    this.currentLatitude = latitude;
    this.currentLongitude = longitude;
    this.lastLocationUpdate = LocalDateTime.now();
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
}
