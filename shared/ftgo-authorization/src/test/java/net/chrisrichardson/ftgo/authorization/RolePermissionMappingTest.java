package net.chrisrichardson.ftgo.authorization;

import net.chrisrichardson.ftgo.authorization.model.FtgoPermission;
import net.chrisrichardson.ftgo.authorization.model.FtgoRole;
import net.chrisrichardson.ftgo.authorization.model.RolePermissionMapping;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link RolePermissionMapping}.
 */
public class RolePermissionMappingTest {

    // -------------------------------------------------------------------------
    // CUSTOMER Role Permissions
    // -------------------------------------------------------------------------

    @Test
    public void customerShouldHaveConsumerPermissions() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.CUSTOMER);
        assertTrue(perms.contains(FtgoPermission.CONSUMER_CREATE));
        assertTrue(perms.contains(FtgoPermission.CONSUMER_READ));
    }

    @Test
    public void customerShouldHaveOrderPermissions() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.CUSTOMER);
        assertTrue(perms.contains(FtgoPermission.ORDER_CREATE));
        assertTrue(perms.contains(FtgoPermission.ORDER_READ));
        assertTrue(perms.contains(FtgoPermission.ORDER_CANCEL));
    }

    @Test
    public void customerShouldReadRestaurants() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.CUSTOMER);
        assertTrue(perms.contains(FtgoPermission.RESTAURANT_READ));
    }

    @Test
    public void customerShouldTrackDeliveries() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.CUSTOMER);
        assertTrue(perms.contains(FtgoPermission.DELIVERY_READ));
    }

    @Test
    public void customerShouldNotModifyRestaurants() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.CUSTOMER);
        assertFalse(perms.contains(FtgoPermission.RESTAURANT_CREATE));
        assertFalse(perms.contains(FtgoPermission.RESTAURANT_UPDATE));
        assertFalse(perms.contains(FtgoPermission.RESTAURANT_DELETE));
    }

    @Test
    public void customerShouldNotManageCouriers() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.CUSTOMER);
        assertFalse(perms.contains(FtgoPermission.COURIER_READ));
        assertFalse(perms.contains(FtgoPermission.COURIER_UPDATE));
    }

    @Test
    public void customerShouldNotUpdateDeliveries() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.CUSTOMER);
        assertFalse(perms.contains(FtgoPermission.DELIVERY_UPDATE));
    }

    // -------------------------------------------------------------------------
    // RESTAURANT_OWNER Role Permissions
    // -------------------------------------------------------------------------

    @Test
    public void restaurantOwnerShouldHaveFullRestaurantCrud() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.RESTAURANT_OWNER);
        assertTrue(perms.contains(FtgoPermission.RESTAURANT_CREATE));
        assertTrue(perms.contains(FtgoPermission.RESTAURANT_READ));
        assertTrue(perms.contains(FtgoPermission.RESTAURANT_UPDATE));
        assertTrue(perms.contains(FtgoPermission.RESTAURANT_DELETE));
    }

    @Test
    public void restaurantOwnerShouldViewOrders() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.RESTAURANT_OWNER);
        assertTrue(perms.contains(FtgoPermission.ORDER_READ));
    }

    @Test
    public void restaurantOwnerShouldNotCreateOrders() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.RESTAURANT_OWNER);
        assertFalse(perms.contains(FtgoPermission.ORDER_CREATE));
        assertFalse(perms.contains(FtgoPermission.ORDER_CANCEL));
    }

    @Test
    public void restaurantOwnerShouldNotManageCouriers() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.RESTAURANT_OWNER);
        assertFalse(perms.contains(FtgoPermission.COURIER_READ));
        assertFalse(perms.contains(FtgoPermission.COURIER_UPDATE));
    }

    @Test
    public void restaurantOwnerShouldNotManageConsumers() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.RESTAURANT_OWNER);
        assertFalse(perms.contains(FtgoPermission.CONSUMER_CREATE));
        assertFalse(perms.contains(FtgoPermission.CONSUMER_READ));
    }

    // -------------------------------------------------------------------------
    // COURIER Role Permissions
    // -------------------------------------------------------------------------

    @Test
    public void courierShouldManageOwnProfile() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.COURIER);
        assertTrue(perms.contains(FtgoPermission.COURIER_READ));
        assertTrue(perms.contains(FtgoPermission.COURIER_UPDATE));
    }

    @Test
    public void courierShouldViewOrders() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.COURIER);
        assertTrue(perms.contains(FtgoPermission.ORDER_READ));
    }

    @Test
    public void courierShouldManageDeliveries() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.COURIER);
        assertTrue(perms.contains(FtgoPermission.DELIVERY_READ));
        assertTrue(perms.contains(FtgoPermission.DELIVERY_UPDATE));
    }

    @Test
    public void courierShouldNotCreateOrCancelOrders() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.COURIER);
        assertFalse(perms.contains(FtgoPermission.ORDER_CREATE));
        assertFalse(perms.contains(FtgoPermission.ORDER_CANCEL));
    }

    @Test
    public void courierShouldNotManageRestaurants() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.COURIER);
        assertFalse(perms.contains(FtgoPermission.RESTAURANT_CREATE));
        assertFalse(perms.contains(FtgoPermission.RESTAURANT_UPDATE));
        assertFalse(perms.contains(FtgoPermission.RESTAURANT_DELETE));
    }

    // -------------------------------------------------------------------------
    // ADMIN Role Permissions
    // -------------------------------------------------------------------------

    @Test
    public void adminShouldHaveAllPermissions() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.ADMIN);
        for (FtgoPermission permission : FtgoPermission.values()) {
            assertTrue("ADMIN should have permission: " + permission.getValue(),
                    perms.contains(permission));
        }
    }

    @Test
    public void adminShouldHaveAllThirteenPermissions() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRole(FtgoRole.ADMIN);
        assertEquals(FtgoPermission.values().length, perms.size());
    }

    // -------------------------------------------------------------------------
    // Cross-cutting Tests
    // -------------------------------------------------------------------------

    @Test
    public void shouldReturnAllMappings() {
        Map<FtgoRole, Set<FtgoPermission>> mappings = RolePermissionMapping.getAllMappings();
        assertNotNull(mappings);
        assertEquals(4, mappings.size());
    }

    @Test
    public void roleHasPermissionShouldWork() {
        assertTrue(RolePermissionMapping.roleHasPermission(FtgoRole.CUSTOMER, FtgoPermission.ORDER_CREATE));
        assertFalse(RolePermissionMapping.roleHasPermission(FtgoRole.CUSTOMER, FtgoPermission.RESTAURANT_CREATE));
        assertTrue(RolePermissionMapping.roleHasPermission(FtgoRole.ADMIN, FtgoPermission.RESTAURANT_CREATE));
    }

    @Test
    public void shouldAggregatePermissionsForMultipleRoles() {
        Set<FtgoPermission> perms = RolePermissionMapping.getPermissionsForRoles(
                Arrays.asList(FtgoRole.CUSTOMER, FtgoRole.COURIER));
        // Union of CUSTOMER and COURIER permissions
        assertTrue(perms.contains(FtgoPermission.ORDER_CREATE)); // from CUSTOMER
        assertTrue(perms.contains(FtgoPermission.COURIER_UPDATE)); // from COURIER
        assertTrue(perms.contains(FtgoPermission.DELIVERY_UPDATE)); // from COURIER
    }
}
