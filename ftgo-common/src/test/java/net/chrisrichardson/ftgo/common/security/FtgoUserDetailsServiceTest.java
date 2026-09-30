package net.chrisrichardson.ftgo.common.security;

import org.junit.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FtgoUserDetailsServiceTest {

  private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

  private static FtgoSecurityProperties.User user(String username, String password, FtgoRole role, Long actorId) {
    FtgoSecurityProperties.User user = new FtgoSecurityProperties.User();
    user.setUsername(username);
    user.setPassword(password);
    user.setRole(role);
    user.setActorId(actorId);
    return user;
  }

  @Test
  public void shouldLoadConfiguredUsers() {
    FtgoSecurityProperties properties = new FtgoSecurityProperties();
    properties.setUsers(Arrays.asList(
            user("ops", "{noop}ops-pw", FtgoRole.ADMIN, null),
            user("consumer-42", "{noop}c-pw", FtgoRole.CONSUMER, 42L)));

    FtgoUserDetailsService service = new FtgoUserDetailsService(properties, passwordEncoder);

    FtgoPrincipal consumer = (FtgoPrincipal) service.loadUserByUsername("consumer-42");
    assertEquals(FtgoRole.CONSUMER, consumer.getRole());
    assertEquals(Long.valueOf(42), consumer.getActorId());
    assertTrue(consumer.actsAs(FtgoRole.CONSUMER, 42L));
    assertFalse(consumer.actsAs(FtgoRole.CONSUMER, 43L));
    assertFalse(consumer.actsAs(FtgoRole.RESTAURANT, 42L));
    assertTrue(passwordEncoder.matches("c-pw", consumer.getPassword()));

    FtgoPrincipal admin = (FtgoPrincipal) service.loadUserByUsername("ops");
    assertTrue(admin.isAdmin());
    assertNull(admin.getActorId());
  }

  @Test(expected = UsernameNotFoundException.class)
  public void shouldRejectUnknownUser() {
    FtgoSecurityProperties properties = new FtgoSecurityProperties();
    properties.setUsers(Arrays.asList(user("ops", "{noop}ops-pw", FtgoRole.ADMIN, null)));

    new FtgoUserDetailsService(properties, passwordEncoder).loadUserByUsername("nobody");
  }

  @Test
  public void shouldCreateGeneratedAdminWhenNoUsersConfigured() {
    FtgoUserDetailsService service = new FtgoUserDetailsService(new FtgoSecurityProperties(), passwordEncoder);

    FtgoPrincipal admin = (FtgoPrincipal) service.loadUserByUsername(FtgoUserDetailsService.DEFAULT_ADMIN_USERNAME);
    assertTrue(admin.isAdmin());
    assertFalse(passwordEncoder.matches("admin", admin.getPassword()));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldRejectDuplicateUsernames() {
    FtgoSecurityProperties properties = new FtgoSecurityProperties();
    properties.setUsers(Arrays.asList(
            user("ops", "{noop}a", FtgoRole.ADMIN, null),
            user("ops", "{noop}b", FtgoRole.ADMIN, null)));

    new FtgoUserDetailsService(properties, passwordEncoder);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRequireActorIdForNonAdminRoles() {
    FtgoSecurityProperties properties = new FtgoSecurityProperties();
    properties.setUsers(Arrays.asList(user("courier", "{noop}a", FtgoRole.COURIER, null)));

    new FtgoUserDetailsService(properties, passwordEncoder);
  }
}
