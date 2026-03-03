package net.chrisrichardson.ftgo.authorization;

import net.chrisrichardson.ftgo.authorization.evaluator.FtgoPermissionEvaluator;
import net.chrisrichardson.ftgo.authorization.evaluator.ResourceOwnershipResolver;
import net.chrisrichardson.ftgo.authorization.model.FtgoRole;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link FtgoPermissionEvaluator}.
 *
 * <p>Tests cover permission checks and resource ownership validation
 * for all role/endpoint combinations defined in the FTGO RBAC model.</p>
 */
public class FtgoPermissionEvaluatorTest {

    private FtgoPermissionEvaluator evaluator;
    private ResourceOwnershipResolver orderOwnershipResolver;
    private ResourceOwnershipResolver restaurantOwnershipResolver;
    private ResourceOwnershipResolver consumerOwnershipResolver;
    private ResourceOwnershipResolver courierOwnershipResolver;

    @Before
    public void setUp() {
        // Order ownership: user-1 owns order 100
        orderOwnershipResolver = new ResourceOwnershipResolver() {
            @Override
            public boolean supports(String resourceType) {
                return "order".equals(resourceType);
            }

            @Override
            public boolean isOwner(String userId, Serializable resourceId) {
                return "user-1".equals(userId) && Long.valueOf(100L).equals(resourceId);
            }
        };

        // Restaurant ownership: user-2 owns restaurant 200
        restaurantOwnershipResolver = new ResourceOwnershipResolver() {
            @Override
            public boolean supports(String resourceType) {
                return "restaurant".equals(resourceType);
            }

            @Override
            public boolean isOwner(String userId, Serializable resourceId) {
                return "user-2".equals(userId) && Long.valueOf(200L).equals(resourceId);
            }
        };

        // Consumer ownership: user-1 owns consumer 300
        consumerOwnershipResolver = new ResourceOwnershipResolver() {
            @Override
            public boolean supports(String resourceType) {
                return "consumer".equals(resourceType);
            }

            @Override
            public boolean isOwner(String userId, Serializable resourceId) {
                return "user-1".equals(userId) && Long.valueOf(300L).equals(resourceId);
            }
        };

        // Courier ownership: user-3 owns courier 400
        courierOwnershipResolver = new ResourceOwnershipResolver() {
            @Override
            public boolean supports(String resourceType) {
                return "courier".equals(resourceType);
            }

            @Override
            public boolean isOwner(String userId, Serializable resourceId) {
                return "user-3".equals(userId) && Long.valueOf(400L).equals(resourceId);
            }
        };

        List<ResourceOwnershipResolver> resolvers = Arrays.asList(
                orderOwnershipResolver, restaurantOwnershipResolver,
                consumerOwnershipResolver, courierOwnershipResolver);
        evaluator = new FtgoPermissionEvaluator(resolvers);
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private Authentication createAuth(String userId, String... roles) {
        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return new UsernamePasswordAuthenticationToken(userId, null, authorities);
    }

    // -------------------------------------------------------------------------
    // ADMIN Permission Tests - Full Access
    // -------------------------------------------------------------------------

    @Test
    public void adminShouldHaveAllSimplePermissions() {
        Authentication admin = createAuth("admin-user", FtgoRole.ADMIN.getAuthority());

        assertTrue(evaluator.hasPermission(admin, null, "consumer:create"));
        assertTrue(evaluator.hasPermission(admin, null, "consumer:read"));
        assertTrue(evaluator.hasPermission(admin, null, "order:create"));
        assertTrue(evaluator.hasPermission(admin, null, "order:read"));
        assertTrue(evaluator.hasPermission(admin, null, "order:cancel"));
        assertTrue(evaluator.hasPermission(admin, null, "restaurant:create"));
        assertTrue(evaluator.hasPermission(admin, null, "restaurant:read"));
        assertTrue(evaluator.hasPermission(admin, null, "restaurant:update"));
        assertTrue(evaluator.hasPermission(admin, null, "restaurant:delete"));
        assertTrue(evaluator.hasPermission(admin, null, "courier:read"));
        assertTrue(evaluator.hasPermission(admin, null, "courier:update"));
        assertTrue(evaluator.hasPermission(admin, null, "delivery:read"));
        assertTrue(evaluator.hasPermission(admin, null, "delivery:update"));
    }

    @Test
    public void adminShouldAccessAnyResource() {
        Authentication admin = createAuth("admin-user", FtgoRole.ADMIN.getAuthority());

        // Admin can access any order regardless of ownership
        assertTrue(evaluator.hasPermission(admin, 100L, "order", "read"));
        assertTrue(evaluator.hasPermission(admin, 999L, "order", "read"));

        // Admin can access any restaurant regardless of ownership
        assertTrue(evaluator.hasPermission(admin, 200L, "restaurant", "update"));
        assertTrue(evaluator.hasPermission(admin, 999L, "restaurant", "update"));
    }

    // -------------------------------------------------------------------------
    // CUSTOMER Permission Tests
    // -------------------------------------------------------------------------

    @Test
    public void customerShouldCreateAndReadConsumers() {
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        assertTrue(evaluator.hasPermission(customer, null, "consumer:create"));
        assertTrue(evaluator.hasPermission(customer, null, "consumer:read"));
    }

    @Test
    public void customerShouldCreateReadAndCancelOrders() {
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        assertTrue(evaluator.hasPermission(customer, null, "order:create"));
        assertTrue(evaluator.hasPermission(customer, null, "order:read"));
        assertTrue(evaluator.hasPermission(customer, null, "order:cancel"));
    }

    @Test
    public void customerShouldReadRestaurants() {
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        assertTrue(evaluator.hasPermission(customer, null, "restaurant:read"));
    }

    @Test
    public void customerShouldTrackDeliveries() {
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        assertTrue(evaluator.hasPermission(customer, null, "delivery:read"));
    }

    @Test
    public void customerShouldNotModifyRestaurants() {
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        assertFalse(evaluator.hasPermission(customer, null, "restaurant:create"));
        assertFalse(evaluator.hasPermission(customer, null, "restaurant:update"));
        assertFalse(evaluator.hasPermission(customer, null, "restaurant:delete"));
    }

    @Test
    public void customerShouldNotManageCouriers() {
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        assertFalse(evaluator.hasPermission(customer, null, "courier:read"));
        assertFalse(evaluator.hasPermission(customer, null, "courier:update"));
    }

    @Test
    public void customerShouldNotUpdateDeliveryStatus() {
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        assertFalse(evaluator.hasPermission(customer, null, "delivery:update"));
    }

    // -------------------------------------------------------------------------
    // RESTAURANT_OWNER Permission Tests
    // -------------------------------------------------------------------------

    @Test
    public void restaurantOwnerShouldHaveFullRestaurantCrud() {
        Authentication owner = createAuth("user-2", FtgoRole.RESTAURANT_OWNER.getAuthority());

        assertTrue(evaluator.hasPermission(owner, null, "restaurant:create"));
        assertTrue(evaluator.hasPermission(owner, null, "restaurant:read"));
        assertTrue(evaluator.hasPermission(owner, null, "restaurant:update"));
        assertTrue(evaluator.hasPermission(owner, null, "restaurant:delete"));
    }

    @Test
    public void restaurantOwnerShouldViewOrders() {
        Authentication owner = createAuth("user-2", FtgoRole.RESTAURANT_OWNER.getAuthority());

        assertTrue(evaluator.hasPermission(owner, null, "order:read"));
    }

    @Test
    public void restaurantOwnerShouldNotCreateOrCancelOrders() {
        Authentication owner = createAuth("user-2", FtgoRole.RESTAURANT_OWNER.getAuthority());

        assertFalse(evaluator.hasPermission(owner, null, "order:create"));
        assertFalse(evaluator.hasPermission(owner, null, "order:cancel"));
    }

    @Test
    public void restaurantOwnerShouldNotManageCouriers() {
        Authentication owner = createAuth("user-2", FtgoRole.RESTAURANT_OWNER.getAuthority());

        assertFalse(evaluator.hasPermission(owner, null, "courier:read"));
        assertFalse(evaluator.hasPermission(owner, null, "courier:update"));
    }

    @Test
    public void restaurantOwnerShouldNotManageConsumers() {
        Authentication owner = createAuth("user-2", FtgoRole.RESTAURANT_OWNER.getAuthority());

        assertFalse(evaluator.hasPermission(owner, null, "consumer:create"));
        assertFalse(evaluator.hasPermission(owner, null, "consumer:read"));
    }

    // -------------------------------------------------------------------------
    // COURIER Permission Tests
    // -------------------------------------------------------------------------

    @Test
    public void courierShouldManageOwnProfile() {
        Authentication courier = createAuth("user-3", FtgoRole.COURIER.getAuthority());

        assertTrue(evaluator.hasPermission(courier, null, "courier:read"));
        assertTrue(evaluator.hasPermission(courier, null, "courier:update"));
    }

    @Test
    public void courierShouldViewOrders() {
        Authentication courier = createAuth("user-3", FtgoRole.COURIER.getAuthority());

        assertTrue(evaluator.hasPermission(courier, null, "order:read"));
    }

    @Test
    public void courierShouldManageDeliveries() {
        Authentication courier = createAuth("user-3", FtgoRole.COURIER.getAuthority());

        assertTrue(evaluator.hasPermission(courier, null, "delivery:read"));
        assertTrue(evaluator.hasPermission(courier, null, "delivery:update"));
    }

    @Test
    public void courierShouldNotCreateOrCancelOrders() {
        Authentication courier = createAuth("user-3", FtgoRole.COURIER.getAuthority());

        assertFalse(evaluator.hasPermission(courier, null, "order:create"));
        assertFalse(evaluator.hasPermission(courier, null, "order:cancel"));
    }

    @Test
    public void courierShouldNotManageRestaurants() {
        Authentication courier = createAuth("user-3", FtgoRole.COURIER.getAuthority());

        assertFalse(evaluator.hasPermission(courier, null, "restaurant:create"));
        assertFalse(evaluator.hasPermission(courier, null, "restaurant:update"));
        assertFalse(evaluator.hasPermission(courier, null, "restaurant:delete"));
    }

    @Test
    public void courierShouldNotManageConsumers() {
        Authentication courier = createAuth("user-3", FtgoRole.COURIER.getAuthority());

        assertFalse(evaluator.hasPermission(courier, null, "consumer:create"));
        assertFalse(evaluator.hasPermission(courier, null, "consumer:read"));
    }

    // -------------------------------------------------------------------------
    // Resource Ownership Tests
    // -------------------------------------------------------------------------

    @Test
    public void customerShouldAccessOwnOrder() {
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        assertTrue(evaluator.hasPermission(customer, 100L, "order", "read"));
    }

    @Test
    public void customerShouldNotAccessOtherUsersOrder() {
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        assertFalse(evaluator.hasPermission(customer, 999L, "order", "read"));
    }

    @Test
    public void customerShouldAccessOwnConsumer() {
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        assertTrue(evaluator.hasPermission(customer, 300L, "consumer", "read"));
    }

    @Test
    public void customerShouldNotAccessOtherConsumer() {
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        assertFalse(evaluator.hasPermission(customer, 999L, "consumer", "read"));
    }

    @Test
    public void restaurantOwnerShouldAccessOwnRestaurant() {
        Authentication owner = createAuth("user-2", FtgoRole.RESTAURANT_OWNER.getAuthority());

        assertTrue(evaluator.hasPermission(owner, 200L, "restaurant", "update"));
    }

    @Test
    public void restaurantOwnerShouldNotAccessOtherRestaurant() {
        Authentication owner = createAuth("user-2", FtgoRole.RESTAURANT_OWNER.getAuthority());

        assertFalse(evaluator.hasPermission(owner, 999L, "restaurant", "update"));
    }

    @Test
    public void courierShouldAccessOwnCourierProfile() {
        Authentication courier = createAuth("user-3", FtgoRole.COURIER.getAuthority());

        assertTrue(evaluator.hasPermission(courier, 400L, "courier", "update"));
    }

    @Test
    public void courierShouldNotAccessOtherCourierProfile() {
        Authentication courier = createAuth("user-3", FtgoRole.COURIER.getAuthority());

        assertFalse(evaluator.hasPermission(courier, 999L, "courier", "update"));
    }

    // -------------------------------------------------------------------------
    // Edge Cases and Security Tests
    // -------------------------------------------------------------------------

    @Test
    public void shouldDenyNullAuthentication() {
        assertFalse(evaluator.hasPermission(null, null, "order:create"));
        assertFalse(evaluator.hasPermission(null, 100L, "order", "read"));
    }

    @Test
    public void shouldDenyNullPermission() {
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        assertFalse(evaluator.hasPermission(customer, null, (Object) null));
    }

    @Test
    public void shouldDenyUnknownPermission() {
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        assertFalse(evaluator.hasPermission(customer, null, "unknown:permission"));
    }

    @Test
    public void shouldDenyAccessWhenNoOwnershipResolverRegistered() {
        // Create evaluator with no resolvers
        FtgoPermissionEvaluator noResolverEvaluator = new FtgoPermissionEvaluator(
                Collections.<ResourceOwnershipResolver>emptyList());
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        // Permission check passes but ownership check fails (no resolver)
        assertFalse(noResolverEvaluator.hasPermission(customer, 100L, "order", "read"));
    }

    @Test
    public void shouldDenyAccessWhenPermissionNotGrantedForResourceType() {
        // Customer cannot update restaurants
        Authentication customer = createAuth("user-1", FtgoRole.CUSTOMER.getAuthority());

        assertFalse(evaluator.hasPermission(customer, 200L, "restaurant", "update"));
    }

    @Test
    public void evaluatorWithNullResolversShouldNotThrow() {
        FtgoPermissionEvaluator nullEvaluator = new FtgoPermissionEvaluator(null);
        Authentication admin = createAuth("admin-user", FtgoRole.ADMIN.getAuthority());

        // Admin should still work without resolvers
        assertTrue(nullEvaluator.hasPermission(admin, null, "order:create"));
    }

    @Test
    public void shouldHandleMultipleRolesOnSameUser() {
        // User with both CUSTOMER and COURIER roles
        Authentication multiRole = createAuth("user-1",
                FtgoRole.CUSTOMER.getAuthority(), FtgoRole.COURIER.getAuthority());

        // Should have permissions from both roles
        assertTrue(evaluator.hasPermission(multiRole, null, "order:create")); // from CUSTOMER
        assertTrue(evaluator.hasPermission(multiRole, null, "courier:update")); // from COURIER
        assertTrue(evaluator.hasPermission(multiRole, null, "delivery:update")); // from COURIER
    }
}
