package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiTrackingSecurityProperties {

  private final String username;
  private final String password;

  public ApiTrackingSecurityProperties(
          @Value("${ftgo.tracking.security.username:}") String username,
          @Value("${ftgo.tracking.security.password:}") String password) {
    this.username = username;
    this.password = password;
  }

  public boolean isConfigured() {
    return username != null && !username.isEmpty() && password != null && !password.isEmpty();
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
