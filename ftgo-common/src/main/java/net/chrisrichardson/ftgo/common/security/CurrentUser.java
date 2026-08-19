package net.chrisrichardson.ftgo.common.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class CurrentUser {

  private CurrentUser() {
  }

  public static Optional<FtgoUserDetails> find() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated())
      return Optional.empty();
    Object principal = authentication.getPrincipal();
    return principal instanceof FtgoUserDetails ? Optional.of((FtgoUserDetails) principal) : Optional.empty();
  }

  public static FtgoUserDetails require() {
    return find().orElseThrow(() -> new AccessDeniedException("Not authenticated"));
  }
}
