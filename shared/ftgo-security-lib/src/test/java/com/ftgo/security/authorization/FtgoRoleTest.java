package com.ftgo.security.authorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link FtgoRole}.
 */
class FtgoRoleTest {

    @Test
    @DisplayName("should define exactly 4 roles")
    void shouldDefineExactlyFourRoles() {
        assertThat(FtgoRole.values()).hasSize(4);
    }

    @Test
    @DisplayName("should include CUSTOMER, RESTAURANT_OWNER, COURIER, ADMIN")
    void shouldIncludeAllExpectedRoles() {
        assertThat(FtgoRole.values())
                .extracting(FtgoRole::name)
                .containsExactlyInAnyOrder(
                        "CUSTOMER", "RESTAURANT_OWNER", "COURIER", "ADMIN");
    }

    @Nested
    @DisplayName("Authority Strings")
    class AuthorityStrings {

        @Test
        @DisplayName("CUSTOMER authority should be ROLE_CUSTOMER")
        void customerAuthority() {
            assertThat(FtgoRole.CUSTOMER.getAuthority()).isEqualTo("ROLE_CUSTOMER");
        }

        @Test
        @DisplayName("RESTAURANT_OWNER authority should be ROLE_RESTAURANT_OWNER")
        void restaurantOwnerAuthority() {
            assertThat(FtgoRole.RESTAURANT_OWNER.getAuthority())
                    .isEqualTo("ROLE_RESTAURANT_OWNER");
        }

        @Test
        @DisplayName("COURIER authority should be ROLE_COURIER")
        void courierAuthority() {
            assertThat(FtgoRole.COURIER.getAuthority()).isEqualTo("ROLE_COURIER");
        }

        @Test
        @DisplayName("ADMIN authority should be ROLE_ADMIN")
        void adminAuthority() {
            assertThat(FtgoRole.ADMIN.getAuthority()).isEqualTo("ROLE_ADMIN");
        }

        @ParameterizedTest
        @EnumSource(FtgoRole.class)
        @DisplayName("all authorities should start with ROLE_ prefix")
        void allAuthoritiesShouldHaveRolePrefix(FtgoRole role) {
            assertThat(role.getAuthority()).startsWith("ROLE_");
        }
    }

    @Nested
    @DisplayName("Role Name")
    class RoleName {

        @ParameterizedTest
        @EnumSource(FtgoRole.class)
        @DisplayName("getRoleName should return enum name")
        void getRoleNameShouldReturnEnumName(FtgoRole role) {
            assertThat(role.getRoleName()).isEqualTo(role.name());
        }
    }

    @Nested
    @DisplayName("fromAuthority")
    class FromAuthority {

        @Test
        @DisplayName("should resolve CUSTOMER from authority string")
        void shouldResolveCustomer() {
            assertThat(FtgoRole.fromAuthority("ROLE_CUSTOMER"))
                    .isEqualTo(FtgoRole.CUSTOMER);
        }

        @Test
        @DisplayName("should resolve RESTAURANT_OWNER from authority string")
        void shouldResolveRestaurantOwner() {
            assertThat(FtgoRole.fromAuthority("ROLE_RESTAURANT_OWNER"))
                    .isEqualTo(FtgoRole.RESTAURANT_OWNER);
        }

        @Test
        @DisplayName("should resolve COURIER from authority string")
        void shouldResolveCourier() {
            assertThat(FtgoRole.fromAuthority("ROLE_COURIER"))
                    .isEqualTo(FtgoRole.COURIER);
        }

        @Test
        @DisplayName("should resolve ADMIN from authority string")
        void shouldResolveAdmin() {
            assertThat(FtgoRole.fromAuthority("ROLE_ADMIN"))
                    .isEqualTo(FtgoRole.ADMIN);
        }

        @Test
        @DisplayName("should throw for unknown authority")
        void shouldThrowForUnknownAuthority() {
            assertThatThrownBy(() -> FtgoRole.fromAuthority("ROLE_UNKNOWN"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown role authority");
        }
    }
}
