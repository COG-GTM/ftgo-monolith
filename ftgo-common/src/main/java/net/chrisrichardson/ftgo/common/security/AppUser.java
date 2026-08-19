package net.chrisrichardson.ftgo.common.security;

import javax.persistence.*;

/**
 * A login account. {@code subjectId} identifies the domain entity the account acts
 * for: the consumer id for {@link UserRole#CONSUMER}, the restaurant id for
 * {@link UserRole#RESTAURANT} and the courier id for {@link UserRole#COURIER}.
 */
@Entity
@Table(name = "app_users")
@Access(AccessType.FIELD)
public class AppUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  @Column(name = "subject_id")
  private Long subjectId;

  @Column(nullable = false)
  private boolean enabled = true;

  private AppUser() {
  }

  public AppUser(String username, String passwordHash, UserRole role, Long subjectId) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.role = role;
    this.subjectId = subjectId;
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

  public UserRole getRole() {
    return role;
  }

  public Long getSubjectId() {
    return subjectId;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
