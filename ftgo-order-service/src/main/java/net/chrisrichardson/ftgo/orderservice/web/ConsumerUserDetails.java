package net.chrisrichardson.ftgo.orderservice.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

public class ConsumerUserDetails extends User {

  public static final String ROLE_CONSUMER = "ROLE_CONSUMER";

  private final long consumerId;

  public ConsumerUserDetails(long consumerId, String passwordHash) {
    super(Long.toString(consumerId), passwordHash, AuthorityUtils.createAuthorityList(ROLE_CONSUMER));
    this.consumerId = consumerId;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public static Optional<Long> consumerIdOf(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof ConsumerUserDetails)) {
      return Optional.empty();
    }
    return Optional.of(((ConsumerUserDetails) principal).getConsumerId());
  }
}
