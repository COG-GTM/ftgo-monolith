package net.chrisrichardson.ftgo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class FtgoUserDetailsService implements UserDetailsService {

  private static final Logger logger = LoggerFactory.getLogger(FtgoUserDetailsService.class);
  private static final Pattern BCRYPT_HASH = Pattern.compile("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}\\z");

  private final Map<String, FtgoUserDetails> usersByUsername = new HashMap<>();

  public FtgoUserDetailsService(FtgoSecurityProperties properties) {
    if (properties.getUsers() == null || properties.getUsers().isEmpty()) {
      logger.warn("No FTGO security users are configured; protected routes will reject all requests");
      return;
    }

    for (int i = 0; i < properties.getUsers().size(); i++) {
      FtgoSecurityProperties.User user = properties.getUsers().get(i);
      String location = "ftgo.security.users[" + i + "]";
      if (user == null || !StringUtils.hasText(user.getUsername())) {
        throw new IllegalStateException(location + ".username must not be blank");
      }
      if (usersByUsername.containsKey(user.getUsername())) {
        throw new IllegalStateException(location + ".username is duplicated: " + user.getUsername());
      }
      if (user.getRole() == null) {
        throw new IllegalStateException(location + ".role must be configured");
      }
      if (user.getPasswordHash() == null || !BCRYPT_HASH.matcher(user.getPasswordHash()).matches()) {
        throw new IllegalStateException(location + ".password-hash must be a Spring Security BCrypt hash");
      }
      if (user.getRole() == FtgoRole.CONSUMER && user.getConsumerId() == null) {
        throw new IllegalStateException(location + ".consumer-id is required for CONSUMER users");
      }
      if (user.getRole() != FtgoRole.CONSUMER && user.getConsumerId() != null) {
        throw new IllegalStateException(location + ".consumer-id is only valid for CONSUMER users");
      }

      usersByUsername.put(user.getUsername(), new FtgoUserDetails(user.getUsername(),
              user.getPasswordHash(), user.getRole(), user.getConsumerId()));
    }
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    FtgoUserDetails user = usersByUsername.get(username);
    if (user == null) {
      throw new UsernameNotFoundException("No FTGO user configured for username " + username);
    }
    return new FtgoUserDetails(user.getUsername(), user.getPassword(), user.getRole(), user.getConsumerId());
  }
}
