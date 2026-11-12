package net.chrisrichardson.ftgo.security;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ftgo.security")
public class FtgoSecurityProperties {

  private AdminProperties admin = new AdminProperties();
  private Map<String, UserProperties> users = new LinkedHashMap<>();

  public AdminProperties getAdmin() {
    return admin;
  }

  public void setAdmin(AdminProperties admin) {
    this.admin = admin;
  }

  public Map<String, UserProperties> getUsers() {
    return users;
  }

  public void setUsers(Map<String, UserProperties> users) {
    this.users = users;
  }

  public static class AdminProperties {
    private String username = "admin";
    private String password;

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
  }

  public static class UserProperties {
    private String password;
    private Long consumerId;
    private Long restaurantId;
    private Long courierId;

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public Long getConsumerId() {
      return consumerId;
    }

    public void setConsumerId(Long consumerId) {
      this.consumerId = consumerId;
    }

    public Long getRestaurantId() {
      return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
      this.restaurantId = restaurantId;
    }

    public Long getCourierId() {
      return courierId;
    }

    public void setCourierId(Long courierId) {
      this.courierId = courierId;
    }
  }
}
