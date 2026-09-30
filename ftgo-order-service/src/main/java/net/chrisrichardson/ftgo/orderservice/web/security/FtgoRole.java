package net.chrisrichardson.ftgo.orderservice.web.security;

public enum FtgoRole {
  CONSUMER("consumerId"),
  RESTAURANT("restaurantId"),
  COURIER("courierId");

  private final String idClaim;

  FtgoRole(String idClaim) {
    this.idClaim = idClaim;
  }

  public String getIdClaim() {
    return idClaim;
  }

  public String authority() {
    return "ROLE_" + name();
  }
}
