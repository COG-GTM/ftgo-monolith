package net.chrisrichardson.ftgo.security;

public class CreateAccountResponse {

  private Long id;
  private String username;
  private Role role;
  private Long entityId;

  public CreateAccountResponse() {
  }

  public CreateAccountResponse(Long id, String username, Role role, Long entityId) {
    this.id = id;
    this.username = username;
    this.role = role;
    this.entityId = entityId;
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public Role getRole() {
    return role;
  }

  public Long getEntityId() {
    return entityId;
  }
}
