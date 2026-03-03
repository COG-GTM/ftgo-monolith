package net.chrisrichardson.ftgo.authorization;

import net.chrisrichardson.ftgo.authorization.model.FtgoRole;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link FtgoRole}.
 */
public class FtgoRoleTest {

    @Test
    public void shouldDefineAllFourRoles() {
        FtgoRole[] roles = FtgoRole.values();
        assertEquals(4, roles.length);
    }

    @Test
    public void shouldHaveCorrectAuthorityPrefix() {
        assertEquals("ROLE_CUSTOMER", FtgoRole.CUSTOMER.getAuthority());
        assertEquals("ROLE_RESTAURANT_OWNER", FtgoRole.RESTAURANT_OWNER.getAuthority());
        assertEquals("ROLE_COURIER", FtgoRole.COURIER.getAuthority());
        assertEquals("ROLE_ADMIN", FtgoRole.ADMIN.getAuthority());
    }

    @Test
    public void shouldReturnRoleName() {
        assertEquals("CUSTOMER", FtgoRole.CUSTOMER.getRoleName());
        assertEquals("RESTAURANT_OWNER", FtgoRole.RESTAURANT_OWNER.getRoleName());
        assertEquals("COURIER", FtgoRole.COURIER.getRoleName());
        assertEquals("ADMIN", FtgoRole.ADMIN.getRoleName());
    }

    @Test
    public void shouldHaveDescriptions() {
        for (FtgoRole role : FtgoRole.values()) {
            assertNotNull(role.getDescription());
        }
    }

    @Test
    public void shouldParseFromStringWithoutPrefix() {
        assertEquals(FtgoRole.CUSTOMER, FtgoRole.fromString("CUSTOMER"));
        assertEquals(FtgoRole.RESTAURANT_OWNER, FtgoRole.fromString("RESTAURANT_OWNER"));
        assertEquals(FtgoRole.COURIER, FtgoRole.fromString("COURIER"));
        assertEquals(FtgoRole.ADMIN, FtgoRole.fromString("ADMIN"));
    }

    @Test
    public void shouldParseFromStringWithPrefix() {
        assertEquals(FtgoRole.CUSTOMER, FtgoRole.fromString("ROLE_CUSTOMER"));
        assertEquals(FtgoRole.RESTAURANT_OWNER, FtgoRole.fromString("ROLE_RESTAURANT_OWNER"));
        assertEquals(FtgoRole.COURIER, FtgoRole.fromString("ROLE_COURIER"));
        assertEquals(FtgoRole.ADMIN, FtgoRole.fromString("ROLE_ADMIN"));
    }

    @Test
    public void shouldParseFromLowercaseString() {
        assertEquals(FtgoRole.CUSTOMER, FtgoRole.fromString("customer"));
        assertEquals(FtgoRole.ADMIN, FtgoRole.fromString("admin"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForNullRoleName() {
        FtgoRole.fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForUnknownRole() {
        FtgoRole.fromString("UNKNOWN_ROLE");
    }
}
