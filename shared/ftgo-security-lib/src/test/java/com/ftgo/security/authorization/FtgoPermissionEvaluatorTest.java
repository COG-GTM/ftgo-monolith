package com.ftgo.security.authorization;

import com.ftgo.security.jwt.JwtUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link FtgoPermissionEvaluator}.
 */
class FtgoPermissionEvaluatorTest {

    private FtgoPermissionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new FtgoPermissionEvaluator();
    }

    // ---------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------

    private Authentication createAuthentication(Long userId, String username,
                                                 List<String> roles,
                                                 List<String> permissions) {
        JwtUserDetails userDetails = new JwtUserDetails(userId, username, roles, permissions);
        return new UsernamePasswordAuthenticationToken(
                userDetails, "token",
                roles.stream().map(SimpleGrantedAuthority::new).toList());
    }

    private Authentication createAdminAuthentication() {
        return createAuthentication(1L, "admin",
                List.of("ROLE_ADMIN"),
                List.of("consumer:create", "consumer:view", "consumer:update", "consumer:delete",
                        "order:create", "order:view", "order:cancel", "order:update", "order:delete",
                        "restaurant:create", "restaurant:view", "restaurant:update", "restaurant:delete",
                        "delivery:view", "delivery:update", "delivery:plan", "delivery:delete"));
    }

    private Authentication createCustomerAuthentication(Long userId) {
        return createAuthentication(userId, "customer-" + userId,
                List.of("ROLE_CUSTOMER"),
                List.of("consumer:view", "consumer:update",
                        "order:create", "order:view", "order:cancel",
                        "restaurant:view",
                        "delivery:view"));
    }

    private Authentication createRestaurantOwnerAuthentication(Long userId) {
        return createAuthentication(userId, "owner-" + userId,
                List.of("ROLE_RESTAURANT_OWNER"),
                List.of("order:view",
                        "restaurant:create", "restaurant:view", "restaurant:update",
                        "consumer:view"));
    }

    private Authentication createCourierAuthentication(Long userId) {
        return createAuthentication(userId, "courier-" + userId,
                List.of("ROLE_COURIER"),
                List.of("order:view",
                        "delivery:view", "delivery:update", "delivery:plan"));
    }

    // ---------------------------------------------------------------
    // Tests: hasPermission(auth, targetDomainObject, permission)
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Simple Permission Check (hasPermission with target object)")
    class SimplePermissionCheck {

        @Test
        @DisplayName("should return false for null authentication")
        void shouldReturnFalseForNullAuth() {
            assertThat(evaluator.hasPermission(null, null, "order:create")).isFalse();
        }

        @Test
        @DisplayName("should return false for null permission")
        void shouldReturnFalseForNullPermission() {
            Authentication auth = createCustomerAuthentication(1L);
            assertThat(evaluator.hasPermission(auth, null, null)).isFalse();
        }

        @Test
        @DisplayName("ADMIN should always have permission")
        void adminShouldAlwaysHavePermission() {
            Authentication admin = createAdminAuthentication();
            assertThat(evaluator.hasPermission(admin, null, "order:create")).isTrue();
            assertThat(evaluator.hasPermission(admin, null, "anything:goes")).isTrue();
        }

        @Test
        @DisplayName("CUSTOMER should have their assigned permissions")
        void customerShouldHaveAssignedPermissions() {
            Authentication customer = createCustomerAuthentication(42L);
            assertThat(evaluator.hasPermission(customer, null, "order:create")).isTrue();
            assertThat(evaluator.hasPermission(customer, null, "order:view")).isTrue();
            assertThat(evaluator.hasPermission(customer, null, "restaurant:view")).isTrue();
        }

        @Test
        @DisplayName("CUSTOMER should NOT have admin permissions")
        void customerShouldNotHaveAdminPermissions() {
            Authentication customer = createCustomerAuthentication(42L);
            assertThat(evaluator.hasPermission(customer, null, "consumer:delete")).isFalse();
            assertThat(evaluator.hasPermission(customer, null, "order:delete")).isFalse();
            assertThat(evaluator.hasPermission(customer, null, "restaurant:create")).isFalse();
        }

        @Test
        @DisplayName("RESTAURANT_OWNER should have their assigned permissions")
        void restaurantOwnerShouldHaveAssignedPermissions() {
            Authentication owner = createRestaurantOwnerAuthentication(10L);
            assertThat(evaluator.hasPermission(owner, null, "restaurant:create")).isTrue();
            assertThat(evaluator.hasPermission(owner, null, "restaurant:update")).isTrue();
            assertThat(evaluator.hasPermission(owner, null, "order:view")).isTrue();
        }

        @Test
        @DisplayName("RESTAURANT_OWNER should NOT have delivery permissions")
        void restaurantOwnerShouldNotHaveDeliveryPermissions() {
            Authentication owner = createRestaurantOwnerAuthentication(10L);
            assertThat(evaluator.hasPermission(owner, null, "delivery:update")).isFalse();
            assertThat(evaluator.hasPermission(owner, null, "delivery:plan")).isFalse();
        }

        @Test
        @DisplayName("COURIER should have their assigned permissions")
        void courierShouldHaveAssignedPermissions() {
            Authentication courier = createCourierAuthentication(20L);
            assertThat(evaluator.hasPermission(courier, null, "delivery:update")).isTrue();
            assertThat(evaluator.hasPermission(courier, null, "delivery:plan")).isTrue();
            assertThat(evaluator.hasPermission(courier, null, "order:view")).isTrue();
        }

        @Test
        @DisplayName("COURIER should NOT have order create or restaurant permissions")
        void courierShouldNotHaveOrderCreateOrRestaurantPermissions() {
            Authentication courier = createCourierAuthentication(20L);
            assertThat(evaluator.hasPermission(courier, null, "order:create")).isFalse();
            assertThat(evaluator.hasPermission(courier, null, "restaurant:create")).isFalse();
            assertThat(evaluator.hasPermission(courier, null, "restaurant:update")).isFalse();
        }
    }

    // ---------------------------------------------------------------
    // Tests: hasPermission(auth, targetId, targetType, permission)
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Resource Permission Check (hasPermission with targetId and type)")
    class ResourcePermissionCheck {

        @Test
        @DisplayName("should return false for null authentication")
        void shouldReturnFalseForNullAuth() {
            assertThat(evaluator.hasPermission(null, 1L, "Order", "VIEW")).isFalse();
        }

        @Test
        @DisplayName("should return false for null target type")
        void shouldReturnFalseForNullTargetType() {
            Authentication auth = createCustomerAuthentication(1L);
            assertThat(evaluator.hasPermission(auth, 1L, null, "VIEW")).isFalse();
        }

        @Test
        @DisplayName("ADMIN should always have permission on any resource")
        void adminShouldAlwaysHavePermission() {
            Authentication admin = createAdminAuthentication();
            assertThat(evaluator.hasPermission(admin, 1L, "Order", "CREATE")).isTrue();
            assertThat(evaluator.hasPermission(admin, 1L, "Restaurant", "DELETE")).isTrue();
        }

        @Test
        @DisplayName("CUSTOMER should have permission for order:view")
        void customerShouldHaveOrderViewPermission() {
            Authentication customer = createCustomerAuthentication(42L);
            // No ownership strategy registered, so permission check only
            assertThat(evaluator.hasPermission(customer, 1L, "Order", "VIEW")).isTrue();
        }

        @Test
        @DisplayName("CUSTOMER should NOT have permission for order:delete")
        void customerShouldNotHaveOrderDeletePermission() {
            Authentication customer = createCustomerAuthentication(42L);
            assertThat(evaluator.hasPermission(customer, 1L, "Order", "DELETE")).isFalse();
        }
    }

    // ---------------------------------------------------------------
    // Tests: Resource Ownership Strategy
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Resource Ownership Validation")
    class ResourceOwnershipValidation {

        @BeforeEach
        void setUpOwnershipStrategy() {
            // Register a test ownership strategy for "Order"
            evaluator.registerOwnershipStrategy("Order", new ResourceOwnershipStrategy() {
                @Override
                public boolean isOwner(Long userId, Serializable resourceId) {
                    // Simulate: order 100 belongs to user 42, order 200 belongs to user 99
                    if (resourceId.equals(100L)) {
                        return userId.equals(42L);
                    }
                    if (resourceId.equals(200L)) {
                        return userId.equals(99L);
                    }
                    return false;
                }

                @Override
                public String getResourceType() {
                    return "Order";
                }
            });
        }

        @Test
        @DisplayName("CUSTOMER should access own order")
        void customerShouldAccessOwnOrder() {
            Authentication customer = createCustomerAuthentication(42L);
            assertThat(evaluator.hasPermission(customer, 100L, "Order", "VIEW")).isTrue();
        }

        @Test
        @DisplayName("CUSTOMER should NOT access another user's order")
        void customerShouldNotAccessOtherUsersOrder() {
            Authentication customer = createCustomerAuthentication(42L);
            assertThat(evaluator.hasPermission(customer, 200L, "Order", "VIEW")).isFalse();
        }

        @Test
        @DisplayName("ADMIN should access any order regardless of ownership")
        void adminShouldAccessAnyOrder() {
            Authentication admin = createAdminAuthentication();
            assertThat(evaluator.hasPermission(admin, 100L, "Order", "VIEW")).isTrue();
            assertThat(evaluator.hasPermission(admin, 200L, "Order", "VIEW")).isTrue();
        }

        @Test
        @DisplayName("should grant access when no ownership strategy is registered for type")
        void shouldGrantAccessWithoutOwnershipStrategy() {
            Authentication customer = createCustomerAuthentication(42L);
            // "Restaurant" has no ownership strategy registered
            assertThat(evaluator.hasPermission(customer, 1L, "Restaurant", "VIEW")).isTrue();
        }

        @Test
        @DisplayName("should deny access when user does not have the base permission")
        void shouldDenyAccessWithoutBasePermission() {
            Authentication customer = createCustomerAuthentication(42L);
            // Customer doesn't have order:delete
            assertThat(evaluator.hasPermission(customer, 100L, "Order", "DELETE")).isFalse();
        }
    }

    // ---------------------------------------------------------------
    // Tests: Cross-Role Authorization Matrix
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Cross-Role Authorization Matrix")
    class CrossRoleAuthorizationMatrix {

        @Nested
        @DisplayName("Consumer Service Endpoints")
        class ConsumerServiceEndpoints {

            @Test
            @DisplayName("ADMIN can create consumers")
            void adminCanCreateConsumers() {
                Authentication admin = createAdminAuthentication();
                assertThat(evaluator.hasPermission(admin, null, "consumer:create")).isTrue();
            }

            @Test
            @DisplayName("CUSTOMER can view consumers")
            void customerCanViewConsumers() {
                Authentication customer = createCustomerAuthentication(1L);
                assertThat(evaluator.hasPermission(customer, null, "consumer:view")).isTrue();
            }

            @Test
            @DisplayName("CUSTOMER cannot delete consumers")
            void customerCannotDeleteConsumers() {
                Authentication customer = createCustomerAuthentication(1L);
                assertThat(evaluator.hasPermission(customer, null, "consumer:delete")).isFalse();
            }

            @Test
            @DisplayName("COURIER cannot create consumers")
            void courierCannotCreateConsumers() {
                Authentication courier = createCourierAuthentication(1L);
                assertThat(evaluator.hasPermission(courier, null, "consumer:create")).isFalse();
            }
        }

        @Nested
        @DisplayName("Order Service Endpoints")
        class OrderServiceEndpoints {

            @Test
            @DisplayName("CUSTOMER can create orders")
            void customerCanCreateOrders() {
                Authentication customer = createCustomerAuthentication(1L);
                assertThat(evaluator.hasPermission(customer, null, "order:create")).isTrue();
            }

            @Test
            @DisplayName("CUSTOMER can cancel orders")
            void customerCanCancelOrders() {
                Authentication customer = createCustomerAuthentication(1L);
                assertThat(evaluator.hasPermission(customer, null, "order:cancel")).isTrue();
            }

            @Test
            @DisplayName("RESTAURANT_OWNER can view orders")
            void restaurantOwnerCanViewOrders() {
                Authentication owner = createRestaurantOwnerAuthentication(1L);
                assertThat(evaluator.hasPermission(owner, null, "order:view")).isTrue();
            }

            @Test
            @DisplayName("COURIER can view orders")
            void courierCanViewOrders() {
                Authentication courier = createCourierAuthentication(1L);
                assertThat(evaluator.hasPermission(courier, null, "order:view")).isTrue();
            }

            @Test
            @DisplayName("COURIER cannot create orders")
            void courierCannotCreateOrders() {
                Authentication courier = createCourierAuthentication(1L);
                assertThat(evaluator.hasPermission(courier, null, "order:create")).isFalse();
            }
        }

        @Nested
        @DisplayName("Restaurant Service Endpoints")
        class RestaurantServiceEndpoints {

            @Test
            @DisplayName("RESTAURANT_OWNER can create restaurants")
            void restaurantOwnerCanCreateRestaurants() {
                Authentication owner = createRestaurantOwnerAuthentication(1L);
                assertThat(evaluator.hasPermission(owner, null, "restaurant:create")).isTrue();
            }

            @Test
            @DisplayName("RESTAURANT_OWNER can update restaurants")
            void restaurantOwnerCanUpdateRestaurants() {
                Authentication owner = createRestaurantOwnerAuthentication(1L);
                assertThat(evaluator.hasPermission(owner, null, "restaurant:update")).isTrue();
            }

            @Test
            @DisplayName("CUSTOMER can view restaurants")
            void customerCanViewRestaurants() {
                Authentication customer = createCustomerAuthentication(1L);
                assertThat(evaluator.hasPermission(customer, null, "restaurant:view")).isTrue();
            }

            @Test
            @DisplayName("CUSTOMER cannot create restaurants")
            void customerCannotCreateRestaurants() {
                Authentication customer = createCustomerAuthentication(1L);
                assertThat(evaluator.hasPermission(customer, null, "restaurant:create")).isFalse();
            }

            @Test
            @DisplayName("COURIER cannot manage restaurants")
            void courierCannotManageRestaurants() {
                Authentication courier = createCourierAuthentication(1L);
                assertThat(evaluator.hasPermission(courier, null, "restaurant:create")).isFalse();
                assertThat(evaluator.hasPermission(courier, null, "restaurant:update")).isFalse();
            }
        }

        @Nested
        @DisplayName("Delivery Service Endpoints")
        class DeliveryServiceEndpoints {

            @Test
            @DisplayName("COURIER can update delivery status")
            void courierCanUpdateDelivery() {
                Authentication courier = createCourierAuthentication(1L);
                assertThat(evaluator.hasPermission(courier, null, "delivery:update")).isTrue();
            }

            @Test
            @DisplayName("COURIER can plan deliveries")
            void courierCanPlanDeliveries() {
                Authentication courier = createCourierAuthentication(1L);
                assertThat(evaluator.hasPermission(courier, null, "delivery:plan")).isTrue();
            }

            @Test
            @DisplayName("CUSTOMER can view deliveries")
            void customerCanViewDeliveries() {
                Authentication customer = createCustomerAuthentication(1L);
                assertThat(evaluator.hasPermission(customer, null, "delivery:view")).isTrue();
            }

            @Test
            @DisplayName("CUSTOMER cannot update deliveries")
            void customerCannotUpdateDeliveries() {
                Authentication customer = createCustomerAuthentication(1L);
                assertThat(evaluator.hasPermission(customer, null, "delivery:update")).isFalse();
            }

            @Test
            @DisplayName("RESTAURANT_OWNER cannot manage deliveries")
            void restaurantOwnerCannotManageDeliveries() {
                Authentication owner = createRestaurantOwnerAuthentication(1L);
                assertThat(evaluator.hasPermission(owner, null, "delivery:update")).isFalse();
                assertThat(evaluator.hasPermission(owner, null, "delivery:plan")).isFalse();
            }

            @Test
            @DisplayName("Only ADMIN can delete deliveries")
            void onlyAdminCanDeleteDeliveries() {
                Authentication admin = createAdminAuthentication();
                Authentication customer = createCustomerAuthentication(1L);
                Authentication courier = createCourierAuthentication(1L);

                assertThat(evaluator.hasPermission(admin, null, "delivery:delete")).isTrue();
                assertThat(evaluator.hasPermission(customer, null, "delivery:delete")).isFalse();
                assertThat(evaluator.hasPermission(courier, null, "delivery:delete")).isFalse();
            }
        }
    }
}
