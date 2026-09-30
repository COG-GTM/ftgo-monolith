package net.chrisrichardson.ftgo.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves users from {@link FtgoSecurityProperties}. When no users are configured a single
 * {@code admin} user with a generated password is created and the password is logged once,
 * mirroring Spring Boot's default user behaviour.
 */
public class FtgoUserDetailsService implements UserDetailsService {

  private static final Logger logger = LoggerFactory.getLogger(FtgoUserDetailsService.class);

  public static final String DEFAULT_ADMIN_USERNAME = "admin";

  private final Map<String, FtgoPrincipal> users = new HashMap<>();

  public FtgoUserDetailsService(FtgoSecurityProperties properties, PasswordEncoder passwordEncoder) {
    for (FtgoSecurityProperties.User user : properties.getUsers()) {
      if (isBlank(user.getUsername()) || isBlank(user.getPassword()) || user.getRole() == null)
        throw new IllegalStateException("ftgo.security.users entries need username, password and role");
      if (users.containsKey(user.getUsername()))
        throw new IllegalStateException("Duplicate ftgo.security.users username: " + user.getUsername());
      users.put(user.getUsername(), new FtgoPrincipal(user.getUsername(), user.getPassword(), user.getRole(), user.getActorId()));
    }
    if (users.isEmpty()) {
      String generatedPassword = UUID.randomUUID().toString();
      users.put(DEFAULT_ADMIN_USERNAME, new FtgoPrincipal(DEFAULT_ADMIN_USERNAME,
              passwordEncoder.encode(generatedPassword), FtgoRole.ADMIN, null));
      logger.warn("No ftgo.security.users configured. Using generated password for user '{}': {}",
              DEFAULT_ADMIN_USERNAME, generatedPassword);
    }
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    FtgoPrincipal principal = users.get(username);
    if (principal == null)
      throw new UsernameNotFoundException(username);
    return principal;
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
