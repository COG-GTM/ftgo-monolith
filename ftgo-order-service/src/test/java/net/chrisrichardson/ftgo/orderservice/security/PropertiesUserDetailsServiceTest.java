package net.chrisrichardson.ftgo.orderservice.security;

import org.junit.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PropertiesUserDetailsServiceTest {

  private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

  @Test
  public void shouldLoadConfiguredUsersWithRolesAndConsumerId() {
    FtgoSecurityProperties properties = new FtgoSecurityProperties();
    properties.setUsers(Arrays.asList(
            user("ops", "secret", Collections.singletonList(FtgoRoles.OPERATIONS), null),
            user("alice", "{noop}alice-pw", Collections.singletonList(FtgoRoles.CONSUMER), 42L)));

    PropertiesUserDetailsService service = new PropertiesUserDetailsService(properties, passwordEncoder);

    FtgoUser ops = (FtgoUser) service.loadUserByUsername("ops");
    assertTrue(passwordEncoder.matches("secret", ops.getPassword()));
    assertTrue(ops.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OPERATIONS")));
    assertEquals(Optional.empty(), ops.getConsumerId());

    FtgoUser alice = (FtgoUser) service.loadUserByUsername("alice");
    assertTrue(passwordEncoder.matches("alice-pw", alice.getPassword()));
    assertEquals(Optional.of(42L), alice.getConsumerId());
  }

  @Test(expected = UsernameNotFoundException.class)
  public void shouldRejectUnknownUser() {
    new PropertiesUserDetailsService(new FtgoSecurityProperties(), passwordEncoder).loadUserByUsername("nobody");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectConsumerWithoutConsumerId() {
    FtgoSecurityProperties properties = new FtgoSecurityProperties();
    properties.setUsers(Collections.singletonList(user("bob", "pw", Collections.singletonList(FtgoRoles.CONSUMER), null)));
    new PropertiesUserDetailsService(properties, passwordEncoder);
  }

  private static FtgoSecurityProperties.UserProperties user(String username, String password, java.util.List<String> roles, Long consumerId) {
    FtgoSecurityProperties.UserProperties user = new FtgoSecurityProperties.UserProperties();
    user.setUsername(username);
    user.setPassword(password);
    user.setRoles(roles);
    user.setConsumerId(consumerId);
    return user;
  }
}
