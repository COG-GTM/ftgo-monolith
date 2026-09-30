package net.chrisrichardson.ftgo.orderservice.web.security;

import java.security.Principal;

public class FtgoPrincipal implements Principal {

  private final String subject;
  private final FtgoRole role;
  private final long id;

  public FtgoPrincipal(String subject, FtgoRole role, long id) {
    this.subject = subject;
    this.role = role;
    this.id = id;
  }

  @Override
  public String getName() {
    return subject;
  }

  public FtgoRole getRole() {
    return role;
  }

  public long getId() {
    return id;
  }

  @Override
  public String toString() {
    return role + ":" + id;
  }
}
