package net.chrisrichardson.ftgo.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The restaurant data an order needs, copied from the restaurant service when the order is placed so that
 * orders never join the restaurant service's tables.
 */
@Embeddable
@Access(AccessType.FIELD)
public class RestaurantSnapshot {

  @Column(name = "restaurant_id")
  private Long id;

  @Column(name = "restaurant_name")
  private String name;

  @Column(name = "restaurant_latitude")
  private Double latitude;

  @Column(name = "restaurant_longitude")
  private Double longitude;

  private RestaurantSnapshot() {
  }

  public RestaurantSnapshot(Long id, String name, Double latitude, Double longitude) {
    this.id = id;
    this.name = name;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Double getLatitude() {
    return latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public boolean hasLocation() {
    return latitude != null && longitude != null;
  }
}
