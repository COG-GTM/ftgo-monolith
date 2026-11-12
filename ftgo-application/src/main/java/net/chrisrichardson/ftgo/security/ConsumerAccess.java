package net.chrisrichardson.ftgo.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("consumerAccess")
public class ConsumerAccess {

  public boolean isSelf(Authentication authentication, String consumerId) {
    if (authentication == null || !(authentication.getPrincipal() instanceof FtgoUserDetails)) {
      return false;
    }

    FtgoUserDetails user = (FtgoUserDetails) authentication.getPrincipal();
    if (user.getRole() != FtgoRole.CONSUMER || user.getConsumerId() == null) {
      return false;
    }
    try {
      return user.getConsumerId().equals(Long.parseLong(consumerId));
    } catch (NumberFormatException ex) {
      return false;
    }
  }
}
