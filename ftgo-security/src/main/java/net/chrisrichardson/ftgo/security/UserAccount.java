package net.chrisrichardson.ftgo.security;

import javax.persistence.*;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(name = "entity_id")
  private Long entityId;

  protected UserAccount() {
  }

  public UserAccount(String username, String passwordHash, Role role, Long entityId) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.role = role;
    this.entityId = entityId;
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Role getRole() {
    return role;
  }

  public Long getEntityId() {
    return entityId;
  }
}
