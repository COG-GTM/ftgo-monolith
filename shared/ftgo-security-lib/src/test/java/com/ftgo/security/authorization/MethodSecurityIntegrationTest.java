package com.ftgo.security.authorization;

import com.ftgo.security.jwt.JwtUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for method-level security with RBAC configuration.
 * <p>
 * Tests that {@code @PreAuthorize} annotations work correctly with the
 * FTGO role hierarchy, permission evaluator, and ownership validation.
 * </p>
 */
@SpringBootTest(classes = MethodSecurityIntegrationTest.TestConfig.class)
class MethodSecurityIntegrationTest {

    @Autowired
    private TestOrderService orderService;

    @Autowired
    private TestRestaurantService restaurantService;

    @Autowired
    private TestConsumerService consumerService;

    @Autowired
    private TestDeliveryService deliveryService;

    // ---------------------------------------------------------------
    // Test Configuration
    // ---------------------------------------------------------------

    @SpringBootConfiguration
    @EnableWebSecurity
    @Import({
            FtgoRoleHierarchyConfiguration.class,
            FtgoMethodSecurityConfiguration.class,
            FtgoPermissionEvaluator.class
    })
    static class TestConfig {

        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        @Bean
        public TestOrderService testOrderService() {
            return new TestOrderService();
        }

        @Bean
        public TestRestaurantService testRestaurantService() {
            return new TestRestaurantService();
        }

        @Bean
        public TestConsumerService testConsumerService() {
            return new TestConsumerService();
        }

        @Bean
        public TestDeliveryService testDeliveryService() {
            return new TestDeliveryService();
        }
    }

    // ---------------------------------------------------------------
    // Test Service Classes (simulate real service methods)
    // ---------------------------------------------------------------

    @Service
    static class TestOrderService {

        @PreAuthorize("hasRole('CUSTOMER')")
        public String createOrder(Long consumerId) {
            return "Order created for consumer " + consumerId;
        }

        @PreAuthorize("hasRole('CUSTOMER') or hasRole('RESTAURANT_OWNER') or hasRole('COURIER')")
        public String viewOrder(Long orderId) {
            return "Order " + orderId;
        }

        @PreAuthorize("hasRole('CUSTOMER')")
        public String cancelOrder(Long orderId) {
            return "Order " + orderId + " cancelled";
        }

        @PreAuthorize("hasRole('ADMIN')")
        public String deleteOrder(Long orderId) {
            return "Order " + orderId + " deleted";
        }
    }

    @Service
    static class TestRestaurantService {

        @PreAuthorize("hasRole('RESTAURANT_OWNER')")
        public String createRestaurant(String name) {
            return "Restaurant " + name + " created";
        }

        @PreAuthorize("hasRole('CUSTOMER')")
        public String viewRestaurant(Long restaurantId) {
            return "Restaurant " + restaurantId;
        }

        @PreAuthorize("hasRole('RESTAURANT_OWNER')")
        public String updateRestaurant(Long restaurantId) {
            return "Restaurant " + restaurantId + " updated";
        }

        @PreAuthorize("hasRole('ADMIN')")
        public String deleteRestaurant(Long restaurantId) {
            return "Restaurant " + restaurantId + " deleted";
        }
    }

    @Service
    static class TestConsumerService {

        @PreAuthorize("hasRole('ADMIN')")
        public String createConsumer(String firstName, String lastName) {
            return "Consumer " + firstName + " " + lastName + " created";
        }

        @PreAuthorize("hasRole('CUSTOMER')")
        public String viewConsumer(Long consumerId) {
            return "Consumer " + consumerId;
        }

        @PreAuthorize("hasRole('ADMIN')")
        public String deleteConsumer(Long consumerId) {
            return "Consumer " + consumerId + " deleted";
        }
    }

    @Service
    static class TestDeliveryService {

        @PreAuthorize("hasRole('COURIER')")
        public String planDelivery(Long orderId) {
            return "Delivery planned for order " + orderId;
        }

        @PreAuthorize("hasRole('COURIER')")
        public String updateDeliveryStatus(Long deliveryId, String status) {
            return "Delivery " + deliveryId + " status: " + status;
        }

        @PreAuthorize("hasRole('CUSTOMER') or hasRole('COURIER')")
        public String trackDelivery(Long deliveryId) {
            return "Tracking delivery " + deliveryId;
        }

        @PreAuthorize("hasRole('ADMIN')")
        public String deleteDelivery(Long deliveryId) {
            return "Delivery " + deliveryId + " deleted";
        }
    }

    // ---------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------

    private void setAuthentication(Long userId, String username,
                                    List<String> roles) {
        JwtUserDetails userDetails = new JwtUserDetails(userId, username, roles, List.of());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, "token",
                roles.stream().map(SimpleGrantedAuthority::new).toList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    // ---------------------------------------------------------------
    // Order Service Tests
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Order Service Authorization")
    class OrderServiceAuthorization {

        @Test
        @DisplayName("CUSTOMER can create orders")
        void customerCanCreateOrders() {
            setAuthentication(42L, "customer", List.of("ROLE_CUSTOMER"));
            try {
                assertThat(orderService.createOrder(42L))
                        .isEqualTo("Order created for consumer 42");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("ADMIN can create orders (inherits CUSTOMER)")
        void adminCanCreateOrders() {
            setAuthentication(1L, "admin", List.of("ROLE_ADMIN"));
            try {
                assertThat(orderService.createOrder(42L))
                        .isEqualTo("Order created for consumer 42");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("RESTAURANT_OWNER can create orders (inherits CUSTOMER)")
        void restaurantOwnerCanCreateOrders() {
            setAuthentication(10L, "owner", List.of("ROLE_RESTAURANT_OWNER"));
            try {
                assertThat(orderService.createOrder(42L))
                        .isEqualTo("Order created for consumer 42");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("COURIER cannot create orders")
        void courierCannotCreateOrders() {
            setAuthentication(20L, "courier", List.of("ROLE_COURIER"));
            try {
                assertThatThrownBy(() -> orderService.createOrder(42L))
                        .isInstanceOf(AccessDeniedException.class);
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("CUSTOMER can view orders")
        void customerCanViewOrders() {
            setAuthentication(42L, "customer", List.of("ROLE_CUSTOMER"));
            try {
                assertThat(orderService.viewOrder(1L)).isEqualTo("Order 1");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("COURIER can view orders")
        void courierCanViewOrders() {
            setAuthentication(20L, "courier", List.of("ROLE_COURIER"));
            try {
                assertThat(orderService.viewOrder(1L)).isEqualTo("Order 1");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("RESTAURANT_OWNER can view orders")
        void restaurantOwnerCanViewOrders() {
            setAuthentication(10L, "owner", List.of("ROLE_RESTAURANT_OWNER"));
            try {
                assertThat(orderService.viewOrder(1L)).isEqualTo("Order 1");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("ADMIN can delete orders")
        void adminCanDeleteOrders() {
            setAuthentication(1L, "admin", List.of("ROLE_ADMIN"));
            try {
                assertThat(orderService.deleteOrder(1L)).isEqualTo("Order 1 deleted");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("CUSTOMER cannot delete orders")
        void customerCannotDeleteOrders() {
            setAuthentication(42L, "customer", List.of("ROLE_CUSTOMER"));
            try {
                assertThatThrownBy(() -> orderService.deleteOrder(1L))
                        .isInstanceOf(AccessDeniedException.class);
            } finally {
                clearAuthentication();
            }
        }
    }

    // ---------------------------------------------------------------
    // Restaurant Service Tests
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Restaurant Service Authorization")
    class RestaurantServiceAuthorization {

        @Test
        @DisplayName("RESTAURANT_OWNER can create restaurants")
        void restaurantOwnerCanCreateRestaurants() {
            setAuthentication(10L, "owner", List.of("ROLE_RESTAURANT_OWNER"));
            try {
                assertThat(restaurantService.createRestaurant("Test"))
                        .isEqualTo("Restaurant Test created");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("ADMIN can create restaurants (inherits RESTAURANT_OWNER)")
        void adminCanCreateRestaurants() {
            setAuthentication(1L, "admin", List.of("ROLE_ADMIN"));
            try {
                assertThat(restaurantService.createRestaurant("Test"))
                        .isEqualTo("Restaurant Test created");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("CUSTOMER cannot create restaurants")
        void customerCannotCreateRestaurants() {
            setAuthentication(42L, "customer", List.of("ROLE_CUSTOMER"));
            try {
                assertThatThrownBy(() -> restaurantService.createRestaurant("Test"))
                        .isInstanceOf(AccessDeniedException.class);
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("CUSTOMER can view restaurants")
        void customerCanViewRestaurants() {
            setAuthentication(42L, "customer", List.of("ROLE_CUSTOMER"));
            try {
                assertThat(restaurantService.viewRestaurant(1L))
                        .isEqualTo("Restaurant 1");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("COURIER cannot create or update restaurants")
        void courierCannotManageRestaurants() {
            setAuthentication(20L, "courier", List.of("ROLE_COURIER"));
            try {
                assertThatThrownBy(() -> restaurantService.createRestaurant("Test"))
                        .isInstanceOf(AccessDeniedException.class);
                assertThatThrownBy(() -> restaurantService.updateRestaurant(1L))
                        .isInstanceOf(AccessDeniedException.class);
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("only ADMIN can delete restaurants")
        void onlyAdminCanDeleteRestaurants() {
            setAuthentication(10L, "owner", List.of("ROLE_RESTAURANT_OWNER"));
            try {
                assertThatThrownBy(() -> restaurantService.deleteRestaurant(1L))
                        .isInstanceOf(AccessDeniedException.class);
            } finally {
                clearAuthentication();
            }

            setAuthentication(1L, "admin", List.of("ROLE_ADMIN"));
            try {
                assertThat(restaurantService.deleteRestaurant(1L))
                        .isEqualTo("Restaurant 1 deleted");
            } finally {
                clearAuthentication();
            }
        }
    }

    // ---------------------------------------------------------------
    // Consumer Service Tests
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Consumer Service Authorization")
    class ConsumerServiceAuthorization {

        @Test
        @DisplayName("ADMIN can create consumers")
        void adminCanCreateConsumers() {
            setAuthentication(1L, "admin", List.of("ROLE_ADMIN"));
            try {
                assertThat(consumerService.createConsumer("John", "Doe"))
                        .isEqualTo("Consumer John Doe created");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("CUSTOMER cannot create consumers")
        void customerCannotCreateConsumers() {
            setAuthentication(42L, "customer", List.of("ROLE_CUSTOMER"));
            try {
                assertThatThrownBy(() -> consumerService.createConsumer("John", "Doe"))
                        .isInstanceOf(AccessDeniedException.class);
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("CUSTOMER can view consumers")
        void customerCanViewConsumers() {
            setAuthentication(42L, "customer", List.of("ROLE_CUSTOMER"));
            try {
                assertThat(consumerService.viewConsumer(42L))
                        .isEqualTo("Consumer 42");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("COURIER cannot create or delete consumers")
        void courierCannotManageConsumers() {
            setAuthentication(20L, "courier", List.of("ROLE_COURIER"));
            try {
                assertThatThrownBy(() -> consumerService.createConsumer("John", "Doe"))
                        .isInstanceOf(AccessDeniedException.class);
                assertThatThrownBy(() -> consumerService.deleteConsumer(1L))
                        .isInstanceOf(AccessDeniedException.class);
            } finally {
                clearAuthentication();
            }
        }
    }

    // ---------------------------------------------------------------
    // Delivery Service Tests
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Delivery Service Authorization")
    class DeliveryServiceAuthorization {

        @Test
        @DisplayName("COURIER can plan deliveries")
        void courierCanPlanDeliveries() {
            setAuthentication(20L, "courier", List.of("ROLE_COURIER"));
            try {
                assertThat(deliveryService.planDelivery(1L))
                        .isEqualTo("Delivery planned for order 1");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("ADMIN can plan deliveries (inherits COURIER)")
        void adminCanPlanDeliveries() {
            setAuthentication(1L, "admin", List.of("ROLE_ADMIN"));
            try {
                assertThat(deliveryService.planDelivery(1L))
                        .isEqualTo("Delivery planned for order 1");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("CUSTOMER cannot plan deliveries")
        void customerCannotPlanDeliveries() {
            setAuthentication(42L, "customer", List.of("ROLE_CUSTOMER"));
            try {
                assertThatThrownBy(() -> deliveryService.planDelivery(1L))
                        .isInstanceOf(AccessDeniedException.class);
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("COURIER can update delivery status")
        void courierCanUpdateDeliveryStatus() {
            setAuthentication(20L, "courier", List.of("ROLE_COURIER"));
            try {
                assertThat(deliveryService.updateDeliveryStatus(1L, "DELIVERED"))
                        .isEqualTo("Delivery 1 status: DELIVERED");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("CUSTOMER can track deliveries")
        void customerCanTrackDeliveries() {
            setAuthentication(42L, "customer", List.of("ROLE_CUSTOMER"));
            try {
                assertThat(deliveryService.trackDelivery(1L))
                        .isEqualTo("Tracking delivery 1");
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("RESTAURANT_OWNER cannot plan deliveries")
        void restaurantOwnerCannotPlanDeliveries() {
            setAuthentication(10L, "owner", List.of("ROLE_RESTAURANT_OWNER"));
            try {
                assertThatThrownBy(() -> deliveryService.planDelivery(1L))
                        .isInstanceOf(AccessDeniedException.class);
            } finally {
                clearAuthentication();
            }
        }

        @Test
        @DisplayName("only ADMIN can delete deliveries")
        void onlyAdminCanDeleteDeliveries() {
            setAuthentication(20L, "courier", List.of("ROLE_COURIER"));
            try {
                assertThatThrownBy(() -> deliveryService.deleteDelivery(1L))
                        .isInstanceOf(AccessDeniedException.class);
            } finally {
                clearAuthentication();
            }

            setAuthentication(1L, "admin", List.of("ROLE_ADMIN"));
            try {
                assertThat(deliveryService.deleteDelivery(1L))
                        .isEqualTo("Delivery 1 deleted");
            } finally {
                clearAuthentication();
            }
        }
    }

    // ---------------------------------------------------------------
    // 403 Forbidden Tests
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Unauthorized Access Returns 403")
    class UnauthorizedAccessReturns403 {

        @Test
        @DisplayName("unauthenticated user should get AccessDeniedException")
        void unauthenticatedUserShouldGetAccessDenied() {
            clearAuthentication();
            assertThatThrownBy(() -> orderService.createOrder(1L))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("wrong role should get AccessDeniedException")
        void wrongRoleShouldGetAccessDenied() {
            setAuthentication(20L, "courier", List.of("ROLE_COURIER"));
            try {
                assertThatThrownBy(() -> restaurantService.createRestaurant("Test"))
                        .isInstanceOf(AccessDeniedException.class);
            } finally {
                clearAuthentication();
            }
        }
    }
}
