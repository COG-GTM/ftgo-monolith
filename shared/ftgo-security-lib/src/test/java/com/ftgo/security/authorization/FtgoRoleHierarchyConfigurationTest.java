package com.ftgo.security.authorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link FtgoRoleHierarchyConfiguration}.
 */
class FtgoRoleHierarchyConfigurationTest {

    private RoleHierarchy roleHierarchy;

    @BeforeEach
    void setUp() {
        FtgoRoleHierarchyConfiguration config = new FtgoRoleHierarchyConfiguration();
        roleHierarchy = config.roleHierarchy();
    }

    @Nested
    @DisplayName("ADMIN Role Hierarchy")
    class AdminRoleHierarchy {

        @Test
        @DisplayName("ADMIN should inherit RESTAURANT_OWNER authority")
        void adminShouldInheritRestaurantOwner() {
            Collection<? extends GrantedAuthority> reachable =
                    roleHierarchy.getReachableGrantedAuthorities(
                            AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

            assertThat(reachable)
                    .extracting(GrantedAuthority::getAuthority)
                    .contains("ROLE_RESTAURANT_OWNER");
        }

        @Test
        @DisplayName("ADMIN should inherit COURIER authority")
        void adminShouldInheritCourier() {
            Collection<? extends GrantedAuthority> reachable =
                    roleHierarchy.getReachableGrantedAuthorities(
                            AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

            assertThat(reachable)
                    .extracting(GrantedAuthority::getAuthority)
                    .contains("ROLE_COURIER");
        }

        @Test
        @DisplayName("ADMIN should inherit CUSTOMER authority (transitive)")
        void adminShouldInheritCustomerTransitively() {
            Collection<? extends GrantedAuthority> reachable =
                    roleHierarchy.getReachableGrantedAuthorities(
                            AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

            assertThat(reachable)
                    .extracting(GrantedAuthority::getAuthority)
                    .contains("ROLE_CUSTOMER");
        }

        @Test
        @DisplayName("ADMIN should have access to all roles")
        void adminShouldHaveAllRoles() {
            Collection<? extends GrantedAuthority> reachable =
                    roleHierarchy.getReachableGrantedAuthorities(
                            AuthorityUtils.createAuthorityList("ROLE_ADMIN"));

            assertThat(reachable)
                    .extracting(GrantedAuthority::getAuthority)
                    .containsAll(List.of(
                            "ROLE_ADMIN",
                            "ROLE_RESTAURANT_OWNER",
                            "ROLE_COURIER",
                            "ROLE_CUSTOMER"));
        }
    }

    @Nested
    @DisplayName("RESTAURANT_OWNER Role Hierarchy")
    class RestaurantOwnerRoleHierarchy {

        @Test
        @DisplayName("RESTAURANT_OWNER should inherit CUSTOMER authority")
        void restaurantOwnerShouldInheritCustomer() {
            Collection<? extends GrantedAuthority> reachable =
                    roleHierarchy.getReachableGrantedAuthorities(
                            AuthorityUtils.createAuthorityList("ROLE_RESTAURANT_OWNER"));

            assertThat(reachable)
                    .extracting(GrantedAuthority::getAuthority)
                    .contains("ROLE_CUSTOMER");
        }

        @Test
        @DisplayName("RESTAURANT_OWNER should NOT inherit COURIER authority")
        void restaurantOwnerShouldNotInheritCourier() {
            Collection<? extends GrantedAuthority> reachable =
                    roleHierarchy.getReachableGrantedAuthorities(
                            AuthorityUtils.createAuthorityList("ROLE_RESTAURANT_OWNER"));

            assertThat(reachable)
                    .extracting(GrantedAuthority::getAuthority)
                    .doesNotContain("ROLE_COURIER");
        }

        @Test
        @DisplayName("RESTAURANT_OWNER should NOT inherit ADMIN authority")
        void restaurantOwnerShouldNotInheritAdmin() {
            Collection<? extends GrantedAuthority> reachable =
                    roleHierarchy.getReachableGrantedAuthorities(
                            AuthorityUtils.createAuthorityList("ROLE_RESTAURANT_OWNER"));

            assertThat(reachable)
                    .extracting(GrantedAuthority::getAuthority)
                    .doesNotContain("ROLE_ADMIN");
        }
    }

    @Nested
    @DisplayName("COURIER Role Hierarchy")
    class CourierRoleHierarchy {

        @Test
        @DisplayName("COURIER should NOT inherit CUSTOMER authority")
        void courierShouldNotInheritCustomer() {
            Collection<? extends GrantedAuthority> reachable =
                    roleHierarchy.getReachableGrantedAuthorities(
                            AuthorityUtils.createAuthorityList("ROLE_COURIER"));

            assertThat(reachable)
                    .extracting(GrantedAuthority::getAuthority)
                    .doesNotContain("ROLE_CUSTOMER");
        }

        @Test
        @DisplayName("COURIER should NOT inherit RESTAURANT_OWNER authority")
        void courierShouldNotInheritRestaurantOwner() {
            Collection<? extends GrantedAuthority> reachable =
                    roleHierarchy.getReachableGrantedAuthorities(
                            AuthorityUtils.createAuthorityList("ROLE_COURIER"));

            assertThat(reachable)
                    .extracting(GrantedAuthority::getAuthority)
                    .doesNotContain("ROLE_RESTAURANT_OWNER");
        }

        @Test
        @DisplayName("COURIER should only have COURIER authority")
        void courierShouldOnlyHaveCourierAuthority() {
            Collection<? extends GrantedAuthority> reachable =
                    roleHierarchy.getReachableGrantedAuthorities(
                            AuthorityUtils.createAuthorityList("ROLE_COURIER"));

            assertThat(reachable)
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_COURIER");
        }
    }

    @Nested
    @DisplayName("CUSTOMER Role Hierarchy")
    class CustomerRoleHierarchy {

        @Test
        @DisplayName("CUSTOMER is the base role with no inheritance")
        void customerShouldBeBaseRole() {
            Collection<? extends GrantedAuthority> reachable =
                    roleHierarchy.getReachableGrantedAuthorities(
                            AuthorityUtils.createAuthorityList("ROLE_CUSTOMER"));

            assertThat(reachable)
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_CUSTOMER");
        }
    }
}
