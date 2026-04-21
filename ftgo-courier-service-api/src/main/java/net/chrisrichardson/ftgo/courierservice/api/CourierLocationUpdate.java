package net.chrisrichardson.ftgo.courierservice.api;

public class CourierLocationUpdate {

  private double latitude;
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
