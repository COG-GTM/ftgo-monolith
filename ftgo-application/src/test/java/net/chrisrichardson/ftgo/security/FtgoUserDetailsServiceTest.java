package net.chrisrichardson.ftgo.security;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FtgoUserDetailsServiceTest {

  private static final String VALID_HASH = "$2a$04$MujfuO4VqzMgeLfEbTg92uqwnrFuXaVu3tpnZ.l4jMnJJTaG1.Cya";

  @Test
  public void rejectsInvalidPasswordHash() {
    FtgoSecurityProperties.User user = user("operator", FtgoRole.OPERATOR, null);
    user.setPasswordHash("plaintext");
    assertInvalid(user, "password-hash");
  }

  @Test
  public void rejectsMissingRole() {
    assertInvalid(user("operator", null, null), "role");
  }

  @Test
  public void rejectsDuplicateUsername() {
    assertInvalid(new FtgoSecurityProperties.User[] {
            user("same", FtgoRole.OPERATOR, null),
            user("same", FtgoRole.RESTAURANT_OWNER, null)
    }, "duplicated");
  }

  @Test
  public void rejectsConsumerWithoutConsumerId() {
    assertInvalid(user("consumer", FtgoRole.CONSUMER, null), "consumer-id");
  }

  @Test
  public void rejectsOperatorWithConsumerId() {
    assertInvalid(user("operator", FtgoRole.OPERATOR, 5L), "consumer-id");
  }

  @Test
  public void rejectsBlankUsername() {
    assertInvalid(user("  ", FtgoRole.OPERATOR, null), "username");
  }

  private static FtgoSecurityProperties.User user(String username, FtgoRole role, Long consumerId) {
    FtgoSecurityProperties.User user = new FtgoSecurityProperties.User();
    user.setUsername(username);
    user.setPasswordHash(VALID_HASH);
    user.setRole(role);
    user.setConsumerId(consumerId);
    return user;
  }

  private static void assertInvalid(FtgoSecurityProperties.User user, String message) {
    assertInvalid(new FtgoSecurityProperties.User[] {user}, message);
  }

  private static void assertInvalid(FtgoSecurityProperties.User[] users, String message) {
    FtgoSecurityProperties properties = new FtgoSecurityProperties();
    properties.setUsers(java.util.Arrays.asList(users));
    try {
      new FtgoUserDetailsService(properties);
      fail("Expected invalid security configuration");
    } catch (IllegalStateException ex) {
      assertTrue(ex.getMessage(), ex.getMessage().contains(message));
    }
  }
}
