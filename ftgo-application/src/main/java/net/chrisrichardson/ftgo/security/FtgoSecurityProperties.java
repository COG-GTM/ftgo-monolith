package net.chrisrichardson.ftgo.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ftgo.security")
public class FtgoSecurityProperties {

  private Account api = new Account();
  private Account operator = new Account();

  public Account getApi() {
    return api;
  }

  public void setApi(Account api) {
    this.api = api;
  }

  public Account getOperator() {
    return operator;
  }

  public void setOperator(Account operator) {
    this.operator = operator;
  }

  public static class Account {
    private String username;
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
}
