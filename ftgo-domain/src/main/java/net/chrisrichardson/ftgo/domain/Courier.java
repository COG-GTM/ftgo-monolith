package net.chrisrichardson.ftgo.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
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

  private String accessTokenHash;

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

  public String issueAccessToken() {
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    this.accessTokenHash = hashAccessToken(token);
    return token;
  }

  public boolean verifyAccessToken(String token) {
    if (accessTokenHash == null || token == null) {
      return false;
    }
    return MessageDigest.isEqual(accessTokenHash.getBytes(StandardCharsets.UTF_8),
            hashAccessToken(token).getBytes(StandardCharsets.UTF_8));
  }

  private static String hashAccessToken(String token) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
