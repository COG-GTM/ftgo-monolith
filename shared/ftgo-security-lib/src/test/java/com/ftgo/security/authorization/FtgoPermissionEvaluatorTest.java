package com.ftgo.security.authorization;

import com.ftgo.security.jwt.FtgoUserDetails;
import com.ftgo.security.jwt.JwtAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link FtgoPermissionEvaluator}.
 *
 * <p>Tests resource ownership validation for both object-based and
 * ID-based permission checks.
 */
class FtgoPermissionEvaluatorTest {

    private FtgoPermissionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new FtgoPermissionEvaluator();
    }

    private JwtAuthenticationToken createAuth(String userId, String... roles) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                .collect(java.util.stream.Collectors.toList());
        FtgoUserDetails userDetails = new FtgoUserDetails(userId, userId, Arrays.asList(roles),
                Collections.emptyMap());
        return new JwtAuthenticationToken(userDetails, "test-token", authorities);
    }

    @Nested
    @DisplayName("Object-based permission evaluation")
    class ObjectBasedPermission {

        @Test
        @DisplayName("Owner can access their own resource")
        void ownerCanAccessOwnResource() {
            JwtAuthenticationToken auth = createAuth("user-1", "ROLE_CUSTOMER");
            TestResourceOwner resource = new TestResourceOwner("user-1");

            assertTrue(evaluator.hasPermission(auth, resource, "VIEW"));
        }

        @Test
        @DisplayName("Non-owner cannot access resource")
        void nonOwnerCannotAccessResource() {
            JwtAuthenticationToken auth = createAuth("user-1", "ROLE_CUSTOMER");
            TestResourceOwner resource = new TestResourceOwner("user-2");

            assertFalse(evaluator.hasPermission(auth, resource, "VIEW"));
        }

        @Test
        @DisplayName("ADMIN bypasses ownership check")
        void adminBypassesOwnershipCheck() {
            JwtAuthenticationToken auth = createAuth("admin-1", "ROLE_ADMIN");
            TestResourceOwner resource = new TestResourceOwner("user-2");

            assertTrue(evaluator.hasPermission(auth, resource, "VIEW"));
        }

        @Test
        @DisplayName("Null authentication returns false")
        void nullAuthenticationReturnsFalse() {
            TestResourceOwner resource = new TestResourceOwner("user-1");

            assertFalse(evaluator.hasPermission(null, resource, "VIEW"));
        }

        @Test
        @DisplayName("Null target returns false")
        void nullTargetReturnsFalse() {
            JwtAuthenticationToken auth = createAuth("user-1", "ROLE_CUSTOMER");

            assertFalse(evaluator.hasPermission(auth, null, "VIEW"));
        }

        @Test
        @DisplayName("Non-ResourceOwner target returns false")
        void nonResourceOwnerTargetReturnsFalse() {
            JwtAuthenticationToken auth = createAuth("user-1", "ROLE_CUSTOMER");

            assertFalse(evaluator.hasPermission(auth, "not-a-resource-owner", "VIEW"));
        }
    }

    @Nested
    @DisplayName("ID-based permission evaluation")
    class IdBasedPermission {

        @Test
        @DisplayName("User can access resource with matching ID")
        void userCanAccessResourceWithMatchingId() {
            JwtAuthenticationToken auth = createAuth("user-1", "ROLE_CUSTOMER");

            assertTrue(evaluator.hasPermission(auth, "user-1", "Consumer", "VIEW"));
        }

        @Test
        @DisplayName("User cannot access resource with non-matching ID")
        void userCannotAccessResourceWithNonMatchingId() {
            JwtAuthenticationToken auth = createAuth("user-1", "ROLE_CUSTOMER");

            assertFalse(evaluator.hasPermission(auth, "user-2", "Consumer", "VIEW"));
        }

        @Test
        @DisplayName("ADMIN bypasses ID check")
        void adminBypassesIdCheck() {
            JwtAuthenticationToken auth = createAuth("admin-1", "ROLE_ADMIN");

            assertTrue(evaluator.hasPermission(auth, "user-2", "Consumer", "VIEW"));
        }

        @Test
        @DisplayName("Null authentication returns false")
        void nullAuthenticationReturnsFalse() {
            assertFalse(evaluator.hasPermission(null, "user-1", "Consumer", "VIEW"));
        }

        @Test
        @DisplayName("Null target ID returns false")
        void nullTargetIdReturnsFalse() {
            JwtAuthenticationToken auth = createAuth("user-1", "ROLE_CUSTOMER");

            assertFalse(evaluator.hasPermission(auth, null, "Consumer", "VIEW"));
        }
    }

    @Nested
    @DisplayName("Various permission types")
    class VariousPermissions {

        @Test
        @DisplayName("Owner can VIEW their resource")
        void ownerCanViewResource() {
            JwtAuthenticationToken auth = createAuth("user-1", "ROLE_CUSTOMER");

            assertTrue(evaluator.hasPermission(auth, "user-1", "Order", "VIEW"));
        }

        @Test
        @DisplayName("Owner can CANCEL their order")
        void ownerCanCancelOrder() {
            JwtAuthenticationToken auth = createAuth("user-1", "ROLE_CUSTOMER");

            assertTrue(evaluator.hasPermission(auth, "user-1", "Order", "CANCEL"));
        }

        @Test
        @DisplayName("Non-owner cannot CANCEL others' orders")
        void nonOwnerCannotCancelOrder() {
            JwtAuthenticationToken auth = createAuth("user-1", "ROLE_CUSTOMER");

            assertFalse(evaluator.hasPermission(auth, "user-2", "Order", "CANCEL"));
        }

        @Test
        @DisplayName("Restaurant owner can UPDATE their restaurant")
        void restaurantOwnerCanUpdateOwnRestaurant() {
            JwtAuthenticationToken auth = createAuth("owner-1", "ROLE_RESTAURANT_OWNER");

            assertTrue(evaluator.hasPermission(auth, "owner-1", "Restaurant", "UPDATE"));
        }

        @Test
        @DisplayName("Restaurant owner cannot UPDATE other restaurants")
        void restaurantOwnerCannotUpdateOtherRestaurant() {
            JwtAuthenticationToken auth = createAuth("owner-1", "ROLE_RESTAURANT_OWNER");

            assertFalse(evaluator.hasPermission(auth, "owner-2", "Restaurant", "UPDATE"));
        }
    }

    /**
     * Simple test implementation of {@link ResourceOwner}.
     */
    static class TestResourceOwner implements ResourceOwner {
        private final String ownerId;

        TestResourceOwner(String ownerId) {
            this.ownerId = ownerId;
        }

        @Override
        public String getOwnerId() {
            return ownerId;
        }
    }
}
