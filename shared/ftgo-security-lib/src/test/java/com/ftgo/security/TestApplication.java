package com.ftgo.security;

import com.ftgo.security.authorization.ResourceOwner;
import com.ftgo.security.util.SecurityUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shared test application for ftgo-security-lib integration tests.
 *
 * <p>Scans the {@code com.ftgo.security} package to pick up all
 * security configuration classes, handlers, and utilities.
 *
 * <p>Provides test endpoints for verifying:
 * <ul>
 *   <li>Basic authentication ({@code /api/test})</li>
 *   <li>User context propagation ({@code /api/test/user-context})</li>
 *   <li>Authorities/roles ({@code /api/test/authorities})</li>
 *   <li>Role-based authorization ({@code /api/test/admin-only}, etc.)</li>
 *   <li>Resource ownership ({@code /api/test/consumers/{id}}, etc.)</li>
 * </ul>
 */
@SpringBootApplication
public class TestApplication {

    @RestController
    static class TestController {

        @GetMapping("/api/test")
        public String test() {
            return "OK";
        }

        /**
         * Returns current user context from SecurityContextHolder.
         * Used by JWT integration tests to verify user context propagation.
         */
        @GetMapping("/api/test/user-context")
        public Map<String, Object> userContext() {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("authenticated", SecurityUtils.isAuthenticated());
            context.put("userId", SecurityUtils.getCurrentUserId().orElse(null));
            context.put("username", SecurityUtils.getCurrentUsername().orElse(null));
            context.put("roles", SecurityUtils.getCurrentRoles());
            return context;
        }

        /**
         * Returns current authorities from SecurityContextHolder.
         * Used by JWT integration tests to verify role-based authorities.
         */
        @GetMapping("/api/test/authorities")
        public Map<String, Object> authorities() {
            Map<String, Object> result = new LinkedHashMap<>();
            Collection<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            result.put("authorities", authorities);
            return result;
        }

        // =================================================================
        // Role-based authorization test endpoints
        // =================================================================

        /** Admin-only endpoint */
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/api/test/admin-only")
        public String adminOnly() {
            return "admin-access";
        }

        /** Customer endpoint (accessible by CUSTOMER and above via hierarchy) */
        @PreAuthorize("hasRole('CUSTOMER')")
        @GetMapping("/api/test/customer-area")
        public String customerArea() {
            return "customer-access";
        }

        /** Restaurant owner endpoint */
        @PreAuthorize("hasRole('RESTAURANT_OWNER')")
        @GetMapping("/api/test/restaurant-owner-area")
        public String restaurantOwnerArea() {
            return "restaurant-owner-access";
        }

        /** Courier endpoint */
        @PreAuthorize("hasRole('COURIER')")
        @GetMapping("/api/test/courier-area")
        public String courierArea() {
            return "courier-access";
        }

        // =================================================================
        // Consumer service endpoint simulations
        // =================================================================

        /** Create consumer — requires CUSTOMER role */
        @PreAuthorize("hasRole('CUSTOMER')")
        @PostMapping("/api/test/consumers")
        public String createConsumer() {
            return "consumer-created";
        }

        /** View own consumer — requires CUSTOMER + ownership or ADMIN */
        @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and hasPermission(#id, 'Consumer', 'VIEW'))")
        @GetMapping("/api/test/consumers/{id}")
        public String getConsumer(@PathVariable String id) {
            return "consumer-" + id;
        }

        /** List all consumers — ADMIN only */
        @PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/api/test/consumers")
        public String listConsumers() {
            return "all-consumers";
        }

        // =================================================================
        // Order service endpoint simulations
        // =================================================================

        /** Create order — requires CUSTOMER role */
        @PreAuthorize("hasRole('CUSTOMER')")
        @PostMapping("/api/test/orders")
        public String createOrder() {
            return "order-created";
        }

        /** View order — CUSTOMER (own), RESTAURANT_OWNER, COURIER, or ADMIN */
        @PreAuthorize("hasRole('ADMIN') or hasRole('COURIER') or hasRole('RESTAURANT_OWNER') "
                + "or (hasRole('CUSTOMER') and hasPermission(#id, 'Order', 'VIEW'))")
        @GetMapping("/api/test/orders/{id}")
        public String getOrder(@PathVariable String id) {
            return "order-" + id;
        }

        /** Cancel order — CUSTOMER (own) or ADMIN */
        @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and hasPermission(#id, 'Order', 'CANCEL'))")
        @PostMapping("/api/test/orders/{id}/cancel")
        public String cancelOrder(@PathVariable String id) {
            return "order-cancelled-" + id;
        }

        // =================================================================
        // Restaurant service endpoint simulations
        // =================================================================

        /** View restaurants — CUSTOMER or above */
        @PreAuthorize("hasRole('CUSTOMER')")
        @GetMapping("/api/test/restaurants")
        public String listRestaurants() {
            return "all-restaurants";
        }

        /** View restaurant — CUSTOMER or above */
        @PreAuthorize("hasRole('CUSTOMER')")
        @GetMapping("/api/test/restaurants/{id}")
        public String getRestaurant(@PathVariable String id) {
            return "restaurant-" + id;
        }

        /** Create restaurant — RESTAURANT_OWNER or ADMIN */
        @PreAuthorize("hasRole('RESTAURANT_OWNER')")
        @PostMapping("/api/test/restaurants")
        public String createRestaurant() {
            return "restaurant-created";
        }

        /** Update restaurant — RESTAURANT_OWNER (own) or ADMIN */
        @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') "
                + "and hasPermission(#id, 'Restaurant', 'UPDATE'))")
        @PutMapping("/api/test/restaurants/{id}")
        public String updateRestaurant(@PathVariable String id) {
            return "restaurant-updated-" + id;
        }

        /** Delete restaurant — RESTAURANT_OWNER (own) or ADMIN */
        @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') "
                + "and hasPermission(#id, 'Restaurant', 'DELETE'))")
        @DeleteMapping("/api/test/restaurants/{id}")
        public String deleteRestaurant(@PathVariable String id) {
            return "restaurant-deleted-" + id;
        }

        // =================================================================
        // Courier/Delivery service endpoint simulations
        // =================================================================

        /** Track delivery — CUSTOMER (own), COURIER (assigned), or ADMIN */
        @PreAuthorize("hasRole('ADMIN') or hasRole('COURIER') "
                + "or (hasRole('CUSTOMER') and hasPermission(#id, 'Delivery', 'TRACK'))")
        @GetMapping("/api/test/deliveries/{id}/track")
        public String trackDelivery(@PathVariable String id) {
            return "delivery-tracking-" + id;
        }

        /** Update delivery status — COURIER (assigned) or ADMIN */
        @PreAuthorize("hasRole('ADMIN') or (hasRole('COURIER') "
                + "and hasPermission(#id, 'Delivery', 'UPDATE_STATUS'))")
        @PutMapping("/api/test/deliveries/{id}/status")
        public String updateDeliveryStatus(@PathVariable String id) {
            return "delivery-status-updated-" + id;
        }

        /** View courier's assigned orders — COURIER (own) or ADMIN */
        @PreAuthorize("hasRole('ADMIN') or (hasRole('COURIER') and hasPermission(#id, 'Courier', 'VIEW'))")
        @GetMapping("/api/test/couriers/{id}/orders")
        public String getCourierOrders(@PathVariable String id) {
            return "courier-orders-" + id;
        }
    }

    /**
     * Simple test resource that implements {@link ResourceOwner} for
     * permission evaluator tests.
     */
    static class TestResource implements ResourceOwner {
        private final String ownerId;

        TestResource(String ownerId) {
            this.ownerId = ownerId;
        }

        @Override
        public String getOwnerId() {
            return ownerId;
        }
    }
}
