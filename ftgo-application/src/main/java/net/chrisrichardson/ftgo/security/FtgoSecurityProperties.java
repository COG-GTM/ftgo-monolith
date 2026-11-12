package net.chrisrichardson.ftgo.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("ftgo.security")
public class FtgoSecurityProperties {

  private List<User> users = new ArrayList<>();

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }

  public static class User {
    private String username;
    private String passwordHash;
    private FtgoRole role;
    private Long consumerId;

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPasswordHash() {
      return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
      this.passwordHash = passwordHash;
    }

    public FtgoRole getRole() {
      return role;
    }

    public void setRole(FtgoRole role) {
      this.role = role;
    }

    public Long getConsumerId() {
      return consumerId;
    }

    public void setConsumerId(Long consumerId) {
      this.consumerId = consumerId;
    }
  }
}
