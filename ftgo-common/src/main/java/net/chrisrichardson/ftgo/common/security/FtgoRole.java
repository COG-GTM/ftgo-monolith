package net.chrisrichardson.ftgo.common.security;

public enum FtgoRole {
  ADMIN,
  CONSUMER,
  RESTAURANT,
  COURIER;

  public String authority() {
    return "ROLE_" + name();
  }
}
