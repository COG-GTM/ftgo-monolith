package net.chrisrichardson.ftgo.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Users allowed to call authenticated endpoints, e.g.
 * <pre>
 * ftgo.security.users[0].username=admin
 * ftgo.security.users[0].password={bcrypt}$2a$10$...
 * ftgo.security.users[0].role=ADMIN
 * ftgo.security.users[1].username=consumer-42
 * ftgo.security.users[1].password={noop}secret
 * ftgo.security.users[1].role=CONSUMER
 * ftgo.security.users[1].actorId=42
 * </pre>
 * Passwords use Spring Security's {@code {id}encodedPassword} format.
 */
@ConfigurationProperties(prefix = "ftgo.security")
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
    private String password;
    private FtgoRole role;
    private Long actorId;

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

    public FtgoRole getRole() {
      return role;
    }

    public void setRole(FtgoRole role) {
      this.role = role;
    }

    public Long getActorId() {
      return actorId;
    }

    public void setActorId(Long actorId) {
      this.actorId = actorId;
    }
  }
}
