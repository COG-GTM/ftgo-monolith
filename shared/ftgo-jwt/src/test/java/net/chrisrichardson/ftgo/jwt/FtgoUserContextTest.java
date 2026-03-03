package net.chrisrichardson.ftgo.jwt;

import net.chrisrichardson.ftgo.jwt.model.FtgoUserContext;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link FtgoUserContext}.
 */
public class FtgoUserContextTest {

    @Test
    public void shouldBuildUserContextWithAllFields() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-1")
                .username("john.doe")
                .roles(Arrays.asList("ADMIN", "USER"))
                .permissions(Arrays.asList("order:read", "order:write"))
                .tokenId("token-123")
                .issuer("https://keycloak.ftgo.com/realms/ftgo")
                .build();

        assertEquals("user-1", context.getUserId());
        assertEquals("john.doe", context.getUsername());
        assertEquals(2, context.getRoles().size());
        assertTrue(context.getRoles().contains("ADMIN"));
        assertTrue(context.getRoles().contains("USER"));
        assertEquals(2, context.getPermissions().size());
        assertTrue(context.getPermissions().contains("order:read"));
        assertTrue(context.getPermissions().contains("order:write"));
        assertEquals("token-123", context.getTokenId());
        assertEquals("https://keycloak.ftgo.com/realms/ftgo", context.getIssuer());
    }

    @Test
    public void shouldBuildUserContextWithMinimalFields() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-1")
                .username("john.doe")
                .build();

        assertEquals("user-1", context.getUserId());
        assertEquals("john.doe", context.getUsername());
        assertTrue(context.getRoles().isEmpty());
        assertTrue(context.getPermissions().isEmpty());
        assertNull(context.getTokenId());
        assertNull(context.getIssuer());
    }

    @Test
    public void shouldReturnImmutableRoles() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-1")
                .username("john.doe")
                .roles(Arrays.asList("USER"))
                .build();

        try {
            context.getRoles().add("HACKER");
        } catch (UnsupportedOperationException e) {
            // Expected - roles set is immutable
        }
        // Verify the original set wasn't modified
        assertFalse(context.getRoles().contains("HACKER"));
    }

    @Test
    public void shouldReturnImmutablePermissions() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-1")
                .username("john.doe")
                .permissions(Arrays.asList("order:read"))
                .build();

        try {
            context.getPermissions().add("admin:all");
        } catch (UnsupportedOperationException e) {
            // Expected - permissions set is immutable
        }
        assertFalse(context.getPermissions().contains("admin:all"));
    }

    @Test
    public void shouldCheckHasRole() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-1")
                .username("john.doe")
                .roles(Arrays.asList("ADMIN", "USER"))
                .build();

        assertTrue(context.hasRole("ADMIN"));
        assertTrue(context.hasRole("USER"));
        assertFalse(context.hasRole("MANAGER"));
    }

    @Test
    public void shouldCheckHasPermission() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-1")
                .username("john.doe")
                .permissions(Arrays.asList("order:read", "order:write"))
                .build();

        assertTrue(context.hasPermission("order:read"));
        assertTrue(context.hasPermission("order:write"));
        assertFalse(context.hasPermission("order:delete"));
    }

    @Test
    public void shouldAddRoleViaBuilder() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-1")
                .username("john.doe")
                .addRole("USER")
                .addRole("ADMIN")
                .build();

        assertTrue(context.hasRole("USER"));
        assertTrue(context.hasRole("ADMIN"));
    }

    @Test
    public void shouldAddPermissionViaBuilder() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-1")
                .username("john.doe")
                .addPermission("order:read")
                .addPermission("order:write")
                .build();

        assertTrue(context.hasPermission("order:read"));
        assertTrue(context.hasPermission("order:write"));
    }

    @Test
    public void shouldImplementEquals() {
        FtgoUserContext context1 = FtgoUserContext.builder()
                .userId("user-1")
                .username("john.doe")
                .roles(Arrays.asList("USER"))
                .permissions(Arrays.asList("order:read"))
                .build();

        FtgoUserContext context2 = FtgoUserContext.builder()
                .userId("user-1")
                .username("john.doe")
                .roles(Arrays.asList("USER"))
                .permissions(Arrays.asList("order:read"))
                .build();

        assertEquals(context1, context2);
        assertEquals(context1.hashCode(), context2.hashCode());
    }

    @Test
    public void shouldNotBeEqualWithDifferentUserId() {
        FtgoUserContext context1 = FtgoUserContext.builder()
                .userId("user-1")
                .username("john.doe")
                .build();

        FtgoUserContext context2 = FtgoUserContext.builder()
                .userId("user-2")
                .username("john.doe")
                .build();

        assertNotEquals(context1, context2);
    }

    @Test
    public void shouldHaveToString() {
        FtgoUserContext context = FtgoUserContext.builder()
                .userId("user-1")
                .username("john.doe")
                .roles(Arrays.asList("USER"))
                .permissions(Arrays.asList("order:read"))
                .build();

        String str = context.toString();
        assertNotNull(str);
        assertTrue(str.contains("user-1"));
        assertTrue(str.contains("john.doe"));
    }
}
