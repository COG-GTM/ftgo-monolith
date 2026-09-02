package net.chrisrichardson.ftgo.common.security;

public enum ApiRole {
  RESTAURANT("ftgo.security.api-keys.restaurant"),
  COURIER("ftgo.security.api-keys.courier"),
  OPERATOR("ftgo.security.api-keys.operator");

  private final String propertyName;

  ApiRole(String propertyName) {
    this.propertyName = propertyName;
  }

  public String getPropertyName() {
    return propertyName;
  }
}
