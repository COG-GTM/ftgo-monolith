package net.chrisrichardson.ftgo.authorization;

import net.chrisrichardson.ftgo.authorization.config.FtgoRoleHierarchyConfig;
import net.chrisrichardson.ftgo.authorization.model.FtgoRole;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the FTGO role hierarchy.
 *
 * <p>Verifies that the role hierarchy correctly implements:
 * ADMIN &gt; RESTAURANT_OWNER &gt; CUSTOMER and ADMIN &gt; COURIER &gt; CUSTOMER</p>
 */
public class RoleHierarchyTest {

    private RoleHierarchy roleHierarchy;

    @Before
    public void setUp() {
        roleHierarchy = FtgoRoleHierarchyConfig.createRoleHierarchy();
    }

    @Test
    public void shouldCreateRoleHierarchy() {
        assertNotNull(roleHierarchy);
    }

    @Test
    public void adminShouldInheritAllRoles() {
        List<GrantedAuthority> adminAuth = Collections.<GrantedAuthority>singletonList(
                new SimpleGrantedAuthority(FtgoRole.ADMIN.getAuthority()));

        Collection<? extends GrantedAuthority> reachable =
                roleHierarchy.getReachableGrantedAuthorities(adminAuth);

        Set<String> authorities = extractAuthorityStrings(reachable);

        assertTrue("ADMIN should have ROLE_ADMIN", authorities.contains("ROLE_ADMIN"));
        assertTrue("ADMIN should inherit ROLE_RESTAURANT_OWNER",
                authorities.contains("ROLE_RESTAURANT_OWNER"));
        assertTrue("ADMIN should inherit ROLE_COURIER", authorities.contains("ROLE_COURIER"));
        assertTrue("ADMIN should inherit ROLE_CUSTOMER", authorities.contains("ROLE_CUSTOMER"));
    }

    @Test
    public void restaurantOwnerShouldInheritCustomer() {
        List<GrantedAuthority> ownerAuth = Collections.<GrantedAuthority>singletonList(
                new SimpleGrantedAuthority(FtgoRole.RESTAURANT_OWNER.getAuthority()));

        Collection<? extends GrantedAuthority> reachable =
                roleHierarchy.getReachableGrantedAuthorities(ownerAuth);

        Set<String> authorities = extractAuthorityStrings(reachable);

        assertTrue("RESTAURANT_OWNER should have ROLE_RESTAURANT_OWNER",
                authorities.contains("ROLE_RESTAURANT_OWNER"));
        assertTrue("RESTAURANT_OWNER should inherit ROLE_CUSTOMER",
                authorities.contains("ROLE_CUSTOMER"));
    }

    @Test
    public void courierShouldInheritCustomer() {
        List<GrantedAuthority> courierAuth = Collections.<GrantedAuthority>singletonList(
                new SimpleGrantedAuthority(FtgoRole.COURIER.getAuthority()));

        Collection<? extends GrantedAuthority> reachable =
                roleHierarchy.getReachableGrantedAuthorities(courierAuth);

        Set<String> authorities = extractAuthorityStrings(reachable);

        assertTrue("COURIER should have ROLE_COURIER", authorities.contains("ROLE_COURIER"));
        assertTrue("COURIER should inherit ROLE_CUSTOMER", authorities.contains("ROLE_CUSTOMER"));
    }

    @Test
    public void customerShouldNotInheritOtherRoles() {
        List<GrantedAuthority> customerAuth = Collections.<GrantedAuthority>singletonList(
                new SimpleGrantedAuthority(FtgoRole.CUSTOMER.getAuthority()));

        Collection<? extends GrantedAuthority> reachable =
                roleHierarchy.getReachableGrantedAuthorities(customerAuth);

        Set<String> authorities = extractAuthorityStrings(reachable);

        assertTrue("CUSTOMER should have ROLE_CUSTOMER", authorities.contains("ROLE_CUSTOMER"));
        assertTrue("CUSTOMER should not have other roles", authorities.size() == 1);
    }

    @Test
    public void roleHierarchyStringShouldBeCorrect() {
        String hierarchy = FtgoRoleHierarchyConfig.ROLE_HIERARCHY_STRING;
        assertNotNull(hierarchy);
        assertTrue(hierarchy.contains("ROLE_ADMIN > ROLE_RESTAURANT_OWNER"));
        assertTrue(hierarchy.contains("ROLE_ADMIN > ROLE_COURIER"));
        assertTrue(hierarchy.contains("ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER"));
        assertTrue(hierarchy.contains("ROLE_COURIER > ROLE_CUSTOMER"));
    }

    private Set<String> extractAuthorityStrings(
            Collection<? extends GrantedAuthority> authorities) {
        Set<String> result = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            result.add(authority.getAuthority());
        }
        return result;
    }
}
