package net.chrisrichardson.ftgo.orderservice.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "ftgo.security")
public class FtgoSecurityProperties {

  private List<UserProperties> users = new ArrayList<>();

  public List<UserProperties> getUsers() {
    return users;
  }

  public void setUsers(List<UserProperties> users) {
    this.users = users;
  }

  public static class UserProperties {

    private String username;

    private String password;

    private List<String> roles = new ArrayList<>();

    private Long consumerId;

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

    public Long getConsumerId() {
      return consumerId;
    }

    public void setConsumerId(Long consumerId) {
      this.consumerId = consumerId;
    }
  }
}
