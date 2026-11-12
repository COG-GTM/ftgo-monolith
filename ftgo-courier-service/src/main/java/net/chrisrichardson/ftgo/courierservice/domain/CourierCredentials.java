package net.chrisrichardson.ftgo.courierservice.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "courier_credentials")
@Access(AccessType.FIELD)
public class CourierCredentials {

  @Id
  private Long courierId;

  private String username;
  private String passwordHash;

  public CourierCredentials() {
  }

  public CourierCredentials(Long courierId) {
    this.courierId = courierId;
  }

  public void update(String username, String passwordHash) {
    this.username = username;
    this.passwordHash = passwordHash;
  }

  public Long getCourierId() {
    return courierId;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }
}
