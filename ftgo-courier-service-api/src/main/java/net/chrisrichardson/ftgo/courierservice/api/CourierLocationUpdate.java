package net.chrisrichardson.ftgo.courierservice.api;

public class CourierLocationUpdate {

  private Double latitude;
  private Double longitude;

  public CourierLocationUpdate() {
  }

  public CourierLocationUpdate(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }
}
