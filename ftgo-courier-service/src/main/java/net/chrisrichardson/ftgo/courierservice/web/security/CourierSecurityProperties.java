package net.chrisrichardson.ftgo.courierservice.web.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "ftgo.courier.security")
public class CourierSecurityProperties {

  public enum Role {
    DISPATCHER, COURIER
  }

  private List<User> users = new ArrayList<>();

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users = users == null ? new ArrayList<>() : users;
  }

  public static class User {

    private String username;

    /**
     * Encoded password, prefixed with the encoder id, e.g. {bcrypt}$2a$10$...
     */
    private String password;

    private Role role;

    /**
     * Identifies the courier a COURIER account is allowed to act as.
     */
    private Long courierId;

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public Role getRole() {
      return role;
    }

    public void setRole(Role role) {
      this.role = role;
    }

    public Long getCourierId() {
      return courierId;
    }

    public void setCourierId(Long courierId) {
      this.courierId = courierId;
    }
  }
}
