package net.chrisrichardson.ftgo.common.security;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

public class FtgoUser extends User {

  private final FtgoRole role;
  private final Long subjectId;

  public FtgoUser(String username, String password, FtgoRole role, Long subjectId) {
    super(username, password, AuthorityUtils.createAuthorityList("ROLE_" + role.name()));
    this.role = role;
    this.subjectId = subjectId;
  }

  public FtgoRole getRole() {
    return role;
  }

  public Long getSubjectId() {
    return subjectId;
  }

  public boolean isAdmin() {
    return role == FtgoRole.ADMIN;
  }

  public boolean isConsumer(long id) {
    return role == FtgoRole.CONSUMER && subjectId != null && subjectId == id;
  }

  public boolean isRestaurant(long id) {
    return role == FtgoRole.RESTAURANT && subjectId != null && subjectId == id;
  }

  public boolean isCourier(long id) {
    return role == FtgoRole.COURIER && subjectId != null && subjectId == id;
  }
}
