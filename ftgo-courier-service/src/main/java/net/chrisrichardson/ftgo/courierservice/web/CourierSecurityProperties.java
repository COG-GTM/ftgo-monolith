package net.chrisrichardson.ftgo.courierservice.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Courier API accounts, e.g. supplied via environment variables:
 * FTGO_SECURITY_USERS_0_USERNAME, FTGO_SECURITY_USERS_0_PASSWORD ({bcrypt}hash),
 * FTGO_SECURITY_USERS_0_ROLES (DISPATCHER or COURIER), FTGO_SECURITY_USERS_0_COURIER_ID.
 */
@ConfigurationProperties("ftgo.security")
public class CourierSecurityProperties {

  private List<Account> users = new ArrayList<>();

  public List<Account> getUsers() {
    return users;
  }

  public void setUsers(List<Account> users) {
    this.users = users;
  }

  public static class Account {
    private String username;
    private String password;
    private List<String> roles = new ArrayList<>();
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

    public List<String> getRoles() {
      return roles;
    }

    public void setRoles(List<String> roles) {
      this.roles = roles;
    }

    public Long getCourierId() {
      return courierId;
    }

    public void setCourierId(Long courierId) {
      this.courierId = courierId;
    }
  }
}
