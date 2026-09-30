package net.chrisrichardson.ftgo.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves users from {@link FtgoSecurityProperties}. When no users are configured nobody can
 * authenticate, so every protected endpoint rejects all callers until users are supplied.
 */
public class FtgoUserDetailsService implements UserDetailsService {

  private static final Logger logger = LoggerFactory.getLogger(FtgoUserDetailsService.class);

  private final Map<String, FtgoPrincipal> users = new HashMap<>();

  public FtgoUserDetailsService(FtgoSecurityProperties properties) {
    for (FtgoSecurityProperties.User user : properties.getUsers()) {
      if (isBlank(user.getUsername()) || isBlank(user.getPassword()) || user.getRole() == null)
        throw new IllegalStateException("ftgo.security.users entries need username, password and role");
      if (users.containsKey(user.getUsername()))
        throw new IllegalStateException("Duplicate ftgo.security.users username: " + user.getUsername());
      users.put(user.getUsername(), new FtgoPrincipal(user.getUsername(), user.getPassword(), user.getRole(), user.getActorId()));
    }
    if (users.isEmpty())
      logger.warn("No ftgo.security.users configured: state-changing order endpoints will reject every caller");
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
