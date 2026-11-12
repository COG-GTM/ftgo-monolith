package net.chrisrichardson.ftgo.security;

public class CreateAccountRequest {

  private String username;
  private String password;
  private Role role;
  private Long entityId;

  public CreateAccountRequest() {
  }

  public CreateAccountRequest(String username, String password, Role role, Long entityId) {
    this.username = username;
    this.password = password;
    this.role = role;
    this.entityId = entityId;
  }

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

  public Long getEntityId() {
    return entityId;
  }

  public void setEntityId(Long entityId) {
    this.entityId = entityId;
  }
}
