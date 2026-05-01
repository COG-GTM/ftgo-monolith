package net.chrisrichardson.ftgo.courierservice.api;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

public class CourierLocationUpdate {

  @DecimalMin("-90.0")
  @DecimalMax("90.0")
  private double latitude;
  @DecimalMin("-180.0")
  @DecimalMax("180.0")
  private double longitude;

  public CourierLocationUpdate() {
  }

  public CourierLocationUpdate(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }
}
