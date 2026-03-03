package net.chrisrichardson.ftgo.authorization;

import net.chrisrichardson.ftgo.authorization.model.FtgoPermission;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link FtgoPermission}.
 */
public class FtgoPermissionTest {

    @Test
    public void shouldDefineAllPermissions() {
        FtgoPermission[] permissions = FtgoPermission.values();
        // 2 consumer + 3 order + 4 restaurant + 2 courier + 2 delivery = 13
        assertEquals(13, permissions.length);
    }

    @Test
    public void shouldHaveCorrectConsumerPermissionValues() {
        assertEquals("consumer:create", FtgoPermission.CONSUMER_CREATE.getValue());
        assertEquals("consumer:read", FtgoPermission.CONSUMER_READ.getValue());
    }

    @Test
    public void shouldHaveCorrectOrderPermissionValues() {
        assertEquals("order:create", FtgoPermission.ORDER_CREATE.getValue());
        assertEquals("order:read", FtgoPermission.ORDER_READ.getValue());
        assertEquals("order:cancel", FtgoPermission.ORDER_CANCEL.getValue());
    }

    @Test
    public void shouldHaveCorrectRestaurantPermissionValues() {
        assertEquals("restaurant:create", FtgoPermission.RESTAURANT_CREATE.getValue());
        assertEquals("restaurant:read", FtgoPermission.RESTAURANT_READ.getValue());
        assertEquals("restaurant:update", FtgoPermission.RESTAURANT_UPDATE.getValue());
        assertEquals("restaurant:delete", FtgoPermission.RESTAURANT_DELETE.getValue());
    }

    @Test
    public void shouldHaveCorrectCourierPermissionValues() {
        assertEquals("courier:read", FtgoPermission.COURIER_READ.getValue());
        assertEquals("courier:update", FtgoPermission.COURIER_UPDATE.getValue());
    }

    @Test
    public void shouldHaveCorrectDeliveryPermissionValues() {
        assertEquals("delivery:read", FtgoPermission.DELIVERY_READ.getValue());
        assertEquals("delivery:update", FtgoPermission.DELIVERY_UPDATE.getValue());
    }

    @Test
    public void shouldHaveDescriptionsForAll() {
        for (FtgoPermission permission : FtgoPermission.values()) {
            assertNotNull(permission.getDescription());
        }
    }

    @Test
    public void shouldParseFromValue() {
        assertEquals(FtgoPermission.ORDER_CREATE, FtgoPermission.fromValue("order:create"));
        assertEquals(FtgoPermission.RESTAURANT_READ, FtgoPermission.fromValue("restaurant:read"));
        assertEquals(FtgoPermission.DELIVERY_UPDATE, FtgoPermission.fromValue("delivery:update"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForNullValue() {
        FtgoPermission.fromValue(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForUnknownPermission() {
        FtgoPermission.fromValue("unknown:action");
    }
}
