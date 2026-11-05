package net.chrisrichardson.ftgo.common.security;

import java.util.Objects;

public final class AuthenticatedPrincipal {

  private final PrincipalType type;
  private final long id;

  private AuthenticatedPrincipal(PrincipalType type, long id) {
    this.type = type;
    this.id = id;
  }

  public static AuthenticatedPrincipal consumer(long id) {
    return new AuthenticatedPrincipal(PrincipalType.CONSUMER, id);
  }

  public static AuthenticatedPrincipal restaurant(long id) {
    return new AuthenticatedPrincipal(PrincipalType.RESTAURANT, id);
  }

  public static AuthenticatedPrincipal courier(long id) {
    return new AuthenticatedPrincipal(PrincipalType.COURIER, id);
  }

  public PrincipalType getType() {
    return type;
  }

  public long getId() {
    return id;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof AuthenticatedPrincipal)) {
      return false;
    }
    AuthenticatedPrincipal that = (AuthenticatedPrincipal) other;
    return id == that.id && type == that.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, id);
  }

  @Override
  public String toString() {
    return "AuthenticatedPrincipal{type=" + type + ", id=" + id + '}';
  }
}
