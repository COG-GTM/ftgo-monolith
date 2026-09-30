package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.domain.Courier;

public class CourierRegistration {

  private final Courier courier;
  private final String accessToken;

  public CourierRegistration(Courier courier, String accessToken) {
    this.courier = courier;
    this.accessToken = accessToken;
  }

  public Courier getCourier() {
    return courier;
  }

  public String getAccessToken() {
    return accessToken;
  }
}
