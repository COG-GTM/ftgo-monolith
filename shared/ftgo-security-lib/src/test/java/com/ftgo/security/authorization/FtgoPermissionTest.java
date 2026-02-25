package com.ftgo.security.authorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link FtgoPermission}.
 */
class FtgoPermissionTest {

    @Test
    @DisplayName("should define permissions for all bounded contexts")
    void shouldDefinePermissionsForAllContexts() {
        assertThat(FtgoPermission.values()).hasSizeGreaterThanOrEqualTo(16);
    }

    @Nested
    @DisplayName("Consumer Service Permissions")
    class ConsumerPermissions {

        @Test
        @DisplayName("should define consumer CRUD permissions")
        void shouldDefineConsumerPermissions() {
            assertThat(FtgoPermission.CONSUMER_CREATE.getPermission())
                    .isEqualTo("consumer:create");
            assertThat(FtgoPermission.CONSUMER_VIEW.getPermission())
                    .isEqualTo("consumer:view");
            assertThat(FtgoPermission.CONSUMER_UPDATE.getPermission())
                    .isEqualTo("consumer:update");
            assertThat(FtgoPermission.CONSUMER_DELETE.getPermission())
                    .isEqualTo("consumer:delete");
        }
    }

    @Nested
    @DisplayName("Order Service Permissions")
    class OrderPermissions {

        @Test
        @DisplayName("should define order CRUD permissions")
        void shouldDefineOrderPermissions() {
            assertThat(FtgoPermission.ORDER_CREATE.getPermission())
                    .isEqualTo("order:create");
            assertThat(FtgoPermission.ORDER_VIEW.getPermission())
                    .isEqualTo("order:view");
            assertThat(FtgoPermission.ORDER_CANCEL.getPermission())
                    .isEqualTo("order:cancel");
            assertThat(FtgoPermission.ORDER_UPDATE.getPermission())
                    .isEqualTo("order:update");
            assertThat(FtgoPermission.ORDER_DELETE.getPermission())
                    .isEqualTo("order:delete");
        }
    }

    @Nested
    @DisplayName("Restaurant Service Permissions")
    class RestaurantPermissions {

        @Test
        @DisplayName("should define restaurant CRUD permissions")
        void shouldDefineRestaurantPermissions() {
            assertThat(FtgoPermission.RESTAURANT_CREATE.getPermission())
                    .isEqualTo("restaurant:create");
            assertThat(FtgoPermission.RESTAURANT_VIEW.getPermission())
                    .isEqualTo("restaurant:view");
            assertThat(FtgoPermission.RESTAURANT_UPDATE.getPermission())
                    .isEqualTo("restaurant:update");
            assertThat(FtgoPermission.RESTAURANT_DELETE.getPermission())
                    .isEqualTo("restaurant:delete");
        }
    }

    @Nested
    @DisplayName("Delivery Service Permissions")
    class DeliveryPermissions {

        @Test
        @DisplayName("should define delivery permissions")
        void shouldDefineDeliveryPermissions() {
            assertThat(FtgoPermission.DELIVERY_VIEW.getPermission())
                    .isEqualTo("delivery:view");
            assertThat(FtgoPermission.DELIVERY_UPDATE.getPermission())
                    .isEqualTo("delivery:update");
            assertThat(FtgoPermission.DELIVERY_PLAN.getPermission())
                    .isEqualTo("delivery:plan");
            assertThat(FtgoPermission.DELIVERY_DELETE.getPermission())
                    .isEqualTo("delivery:delete");
        }
    }

    @Nested
    @DisplayName("Permission Format")
    class PermissionFormat {

        @ParameterizedTest
        @EnumSource(FtgoPermission.class)
        @DisplayName("all permissions should follow context:operation format")
        void allPermissionsShouldFollowFormat(FtgoPermission permission) {
            assertThat(permission.getPermission()).matches("[a-z]+:[a-z]+");
        }

        @ParameterizedTest
        @EnumSource(FtgoPermission.class)
        @DisplayName("toString should return permission string")
        void toStringShouldReturnPermissionString(FtgoPermission permission) {
            assertThat(permission.toString()).isEqualTo(permission.getPermission());
        }
    }

    @Nested
    @DisplayName("fromString")
    class FromString {

        @Test
        @DisplayName("should resolve permission from string")
        void shouldResolveFromString() {
            assertThat(FtgoPermission.fromString("order:create"))
                    .isEqualTo(FtgoPermission.ORDER_CREATE);
        }

        @Test
        @DisplayName("should throw for unknown permission")
        void shouldThrowForUnknownPermission() {
            assertThatThrownBy(() -> FtgoPermission.fromString("unknown:permission"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown permission");
        }
    }
}
