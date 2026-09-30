package net.chrisrichardson.ftgo.orderservice.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class PropertiesUserDetailsService implements UserDetailsService {

  private final Map<String, FtgoUser> users = new HashMap<>();

  public PropertiesUserDetailsService(FtgoSecurityProperties properties, PasswordEncoder passwordEncoder) {
    for (FtgoSecurityProperties.UserProperties user : properties.getUsers()) {
      if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
        throw new IllegalArgumentException("ftgo.security.users entries require both a username and a password");
      }
      if (user.getRoles().contains(FtgoRoles.CONSUMER) && user.getConsumerId() == null) {
        throw new IllegalArgumentException("ftgo.security.users[" + user.getUsername() + "] has role CONSUMER but no consumerId");
      }
      users.put(user.getUsername(), new FtgoUser(user.getUsername(),
              encodedPassword(user.getPassword(), passwordEncoder),
              user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(toList()),
              user.getConsumerId()));
    }
  }

  private static String encodedPassword(String configured, PasswordEncoder passwordEncoder) {
    return configured.startsWith("{") ? configured : passwordEncoder.encode(configured);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    FtgoUser user = users.get(username);
    if (user == null) {
      throw new UsernameNotFoundException(username);
    }
    return new FtgoUser(user.getUsername(), user.getPassword(), user.getAuthorities(), user.getConsumerId().orElse(null));
  }
}
