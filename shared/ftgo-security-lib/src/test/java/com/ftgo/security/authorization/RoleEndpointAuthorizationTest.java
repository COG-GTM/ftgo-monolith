package com.ftgo.security.authorization;

import com.ftgo.security.TestApplication;
import com.ftgo.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprehensive authorization tests covering all role/endpoint combinations.
 *
 * <p>Tests the full RBAC permission matrix for all four services:
 * Consumer, Order, Restaurant, and Courier/Delivery.
 *
 * <p>Each test verifies that:
 * <ul>
 *   <li>Authorized roles receive 200 OK</li>
 *   <li>Unauthorized roles receive 403 Forbidden</li>
 *   <li>Unauthenticated requests receive 401 Unauthorized</li>
 *   <li>Resource ownership is correctly enforced</li>
 * </ul>
 */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class RoleEndpointAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // User IDs for ownership tests
    private static final String CUSTOMER_ID = "customer-1";
    private static final String OTHER_CUSTOMER_ID = "customer-2";
    private static final String OWNER_ID = "owner-1";
    private static final String OTHER_OWNER_ID = "owner-2";
    private static final String COURIER_ID = "courier-1";
    private static final String ADMIN_ID = "admin-1";

    private String customerToken;
    private String otherCustomerToken;
    private String restaurantOwnerToken;
    private String otherRestaurantOwnerToken;
    private String courierToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        customerToken = jwtTokenProvider.generateAccessToken(
                CUSTOMER_ID, "customer", Collections.singletonList("ROLE_CUSTOMER"));
        otherCustomerToken = jwtTokenProvider.generateAccessToken(
                OTHER_CUSTOMER_ID, "customer2", Collections.singletonList("ROLE_CUSTOMER"));
        restaurantOwnerToken = jwtTokenProvider.generateAccessToken(
                OWNER_ID, "owner", Collections.singletonList("ROLE_RESTAURANT_OWNER"));
        otherRestaurantOwnerToken = jwtTokenProvider.generateAccessToken(
                OTHER_OWNER_ID, "owner2", Collections.singletonList("ROLE_RESTAURANT_OWNER"));
        courierToken = jwtTokenProvider.generateAccessToken(
                COURIER_ID, "courier", Collections.singletonList("ROLE_COURIER"));
        adminToken = jwtTokenProvider.generateAccessToken(
                ADMIN_ID, "admin", Collections.singletonList("ROLE_ADMIN"));
    }

    // =========================================================================
    // Consumer Service Endpoints
    // =========================================================================

    @Nested
    @DisplayName("POST /consumers — Create Consumer")
    class CreateConsumer {

        @Test
        @DisplayName("CUSTOMER can create consumer")
        void customerCanCreateConsumer() throws Exception {
            mockMvc.perform(post("/api/test/consumers")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("consumer-created"));
        }

        @Test
        @DisplayName("RESTAURANT_OWNER can create consumer (inherits CUSTOMER)")
        void restaurantOwnerCanCreateConsumer() throws Exception {
            mockMvc.perform(post("/api/test/consumers")
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can create consumer (inherits all)")
        void adminCanCreateConsumer() throws Exception {
            mockMvc.perform(post("/api/test/consumers")
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("COURIER cannot create consumer")
        void courierCannotCreateConsumer() throws Exception {
            mockMvc.perform(post("/api/test/consumers")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unauthenticated user gets 401")
        void unauthenticatedGets401() throws Exception {
            mockMvc.perform(post("/api/test/consumers")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /consumers/{id} — View Consumer (ownership)")
    class ViewConsumer {

        @Test
        @DisplayName("CUSTOMER can view own consumer")
        void customerCanViewOwnConsumer() throws Exception {
            mockMvc.perform(get("/api/test/consumers/" + CUSTOMER_ID)
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("consumer-" + CUSTOMER_ID));
        }

        @Test
        @DisplayName("CUSTOMER cannot view other's consumer")
        void customerCannotViewOtherConsumer() throws Exception {
            mockMvc.perform(get("/api/test/consumers/" + OTHER_CUSTOMER_ID)
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN can view any consumer")
        void adminCanViewAnyConsumer() throws Exception {
            mockMvc.perform(get("/api/test/consumers/" + CUSTOMER_ID)
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("COURIER cannot view consumer")
        void courierCannotViewConsumer() throws Exception {
            mockMvc.perform(get("/api/test/consumers/" + COURIER_ID)
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /consumers — List Consumers (ADMIN only)")
    class ListConsumers {

        @Test
        @DisplayName("ADMIN can list all consumers")
        void adminCanListConsumers() throws Exception {
            mockMvc.perform(get("/api/test/consumers")
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("all-consumers"));
        }

        @Test
        @DisplayName("CUSTOMER cannot list all consumers")
        void customerCannotListConsumers() throws Exception {
            mockMvc.perform(get("/api/test/consumers")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER cannot list all consumers")
        void restaurantOwnerCannotListConsumers() throws Exception {
            mockMvc.perform(get("/api/test/consumers")
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("COURIER cannot list all consumers")
        void courierCannotListConsumers() throws Exception {
            mockMvc.perform(get("/api/test/consumers")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // Order Service Endpoints
    // =========================================================================

    @Nested
    @DisplayName("POST /orders — Create Order")
    class CreateOrder {

        @Test
        @DisplayName("CUSTOMER can create order")
        void customerCanCreateOrder() throws Exception {
            mockMvc.perform(post("/api/test/orders")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("order-created"));
        }

        @Test
        @DisplayName("RESTAURANT_OWNER can create order (inherits CUSTOMER)")
        void restaurantOwnerCanCreateOrder() throws Exception {
            mockMvc.perform(post("/api/test/orders")
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can create order")
        void adminCanCreateOrder() throws Exception {
            mockMvc.perform(post("/api/test/orders")
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("COURIER cannot create order")
        void courierCannotCreateOrder() throws Exception {
            mockMvc.perform(post("/api/test/orders")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /orders/{id} — View Order")
    class ViewOrder {

        @Test
        @DisplayName("CUSTOMER can view own order")
        void customerCanViewOwnOrder() throws Exception {
            mockMvc.perform(get("/api/test/orders/" + CUSTOMER_ID)
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER cannot view other's order")
        void customerCannotViewOtherOrder() throws Exception {
            mockMvc.perform(get("/api/test/orders/" + OTHER_CUSTOMER_ID)
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER can view orders (related)")
        void restaurantOwnerCanViewOrders() throws Exception {
            mockMvc.perform(get("/api/test/orders/" + CUSTOMER_ID)
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("COURIER can view orders (assigned)")
        void courierCanViewOrders() throws Exception {
            mockMvc.perform(get("/api/test/orders/" + CUSTOMER_ID)
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can view any order")
        void adminCanViewAnyOrder() throws Exception {
            mockMvc.perform(get("/api/test/orders/" + CUSTOMER_ID)
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /orders/{id}/cancel — Cancel Order (ownership)")
    class CancelOrder {

        @Test
        @DisplayName("CUSTOMER can cancel own order")
        void customerCanCancelOwnOrder() throws Exception {
            mockMvc.perform(post("/api/test/orders/" + CUSTOMER_ID + "/cancel")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("order-cancelled-" + CUSTOMER_ID));
        }

        @Test
        @DisplayName("CUSTOMER cannot cancel other's order")
        void customerCannotCancelOtherOrder() throws Exception {
            mockMvc.perform(post("/api/test/orders/" + OTHER_CUSTOMER_ID + "/cancel")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN can cancel any order")
        void adminCanCancelAnyOrder() throws Exception {
            mockMvc.perform(post("/api/test/orders/" + CUSTOMER_ID + "/cancel")
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("COURIER cannot cancel orders")
        void courierCannotCancelOrders() throws Exception {
            mockMvc.perform(post("/api/test/orders/" + COURIER_ID + "/cancel")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER cannot cancel orders directly")
        void restaurantOwnerCannotCancelOrders() throws Exception {
            // RESTAURANT_OWNER inherits CUSTOMER, but still needs ownership
            mockMvc.perform(post("/api/test/orders/" + OTHER_CUSTOMER_ID + "/cancel")
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // Restaurant Service Endpoints
    // =========================================================================

    @Nested
    @DisplayName("GET /restaurants — List Restaurants")
    class ListRestaurants {

        @Test
        @DisplayName("CUSTOMER can list restaurants")
        void customerCanListRestaurants() throws Exception {
            mockMvc.perform(get("/api/test/restaurants")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("all-restaurants"));
        }

        @Test
        @DisplayName("RESTAURANT_OWNER can list restaurants")
        void restaurantOwnerCanListRestaurants() throws Exception {
            mockMvc.perform(get("/api/test/restaurants")
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can list restaurants")
        void adminCanListRestaurants() throws Exception {
            mockMvc.perform(get("/api/test/restaurants")
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("COURIER cannot list restaurants")
        void courierCannotListRestaurants() throws Exception {
            mockMvc.perform(get("/api/test/restaurants")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /restaurants — Create Restaurant")
    class CreateRestaurant {

        @Test
        @DisplayName("RESTAURANT_OWNER can create restaurant")
        void restaurantOwnerCanCreateRestaurant() throws Exception {
            mockMvc.perform(post("/api/test/restaurants")
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("restaurant-created"));
        }

        @Test
        @DisplayName("ADMIN can create restaurant")
        void adminCanCreateRestaurant() throws Exception {
            mockMvc.perform(post("/api/test/restaurants")
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER cannot create restaurant")
        void customerCannotCreateRestaurant() throws Exception {
            mockMvc.perform(post("/api/test/restaurants")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("COURIER cannot create restaurant")
        void courierCannotCreateRestaurant() throws Exception {
            mockMvc.perform(post("/api/test/restaurants")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /restaurants/{id} — Update Restaurant (ownership)")
    class UpdateRestaurant {

        @Test
        @DisplayName("RESTAURANT_OWNER can update own restaurant")
        void restaurantOwnerCanUpdateOwnRestaurant() throws Exception {
            mockMvc.perform(put("/api/test/restaurants/" + OWNER_ID)
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("restaurant-updated-" + OWNER_ID));
        }

        @Test
        @DisplayName("RESTAURANT_OWNER cannot update other's restaurant")
        void restaurantOwnerCannotUpdateOtherRestaurant() throws Exception {
            mockMvc.perform(put("/api/test/restaurants/" + OTHER_OWNER_ID)
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN can update any restaurant")
        void adminCanUpdateAnyRestaurant() throws Exception {
            mockMvc.perform(put("/api/test/restaurants/" + OWNER_ID)
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER cannot update restaurant")
        void customerCannotUpdateRestaurant() throws Exception {
            mockMvc.perform(put("/api/test/restaurants/" + CUSTOMER_ID)
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /restaurants/{id} — Delete Restaurant (ownership)")
    class DeleteRestaurant {

        @Test
        @DisplayName("RESTAURANT_OWNER can delete own restaurant")
        void restaurantOwnerCanDeleteOwnRestaurant() throws Exception {
            mockMvc.perform(delete("/api/test/restaurants/" + OWNER_ID)
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("restaurant-deleted-" + OWNER_ID));
        }

        @Test
        @DisplayName("RESTAURANT_OWNER cannot delete other's restaurant")
        void restaurantOwnerCannotDeleteOtherRestaurant() throws Exception {
            mockMvc.perform(delete("/api/test/restaurants/" + OTHER_OWNER_ID)
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN can delete any restaurant")
        void adminCanDeleteAnyRestaurant() throws Exception {
            mockMvc.perform(delete("/api/test/restaurants/" + OWNER_ID)
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER cannot delete restaurant")
        void customerCannotDeleteRestaurant() throws Exception {
            mockMvc.perform(delete("/api/test/restaurants/" + CUSTOMER_ID)
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // Courier / Delivery Endpoints
    // =========================================================================

    @Nested
    @DisplayName("GET /deliveries/{id}/track — Track Delivery")
    class TrackDelivery {

        @Test
        @DisplayName("CUSTOMER can track own delivery")
        void customerCanTrackOwnDelivery() throws Exception {
            mockMvc.perform(get("/api/test/deliveries/" + CUSTOMER_ID + "/track")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("delivery-tracking-" + CUSTOMER_ID));
        }

        @Test
        @DisplayName("CUSTOMER cannot track other's delivery")
        void customerCannotTrackOtherDelivery() throws Exception {
            mockMvc.perform(get("/api/test/deliveries/" + OTHER_CUSTOMER_ID + "/track")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("COURIER can track deliveries")
        void courierCanTrackDeliveries() throws Exception {
            mockMvc.perform(get("/api/test/deliveries/" + CUSTOMER_ID + "/track")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ADMIN can track any delivery")
        void adminCanTrackAnyDelivery() throws Exception {
            mockMvc.perform(get("/api/test/deliveries/" + CUSTOMER_ID + "/track")
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER cannot track deliveries")
        void restaurantOwnerCannotTrackDeliveries() throws Exception {
            // RESTAURANT_OWNER inherits CUSTOMER but still needs ownership check
            mockMvc.perform(get("/api/test/deliveries/" + OTHER_CUSTOMER_ID + "/track")
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /deliveries/{id}/status — Update Delivery Status")
    class UpdateDeliveryStatus {

        @Test
        @DisplayName("COURIER can update delivery status (assigned)")
        void courierCanUpdateDeliveryStatus() throws Exception {
            mockMvc.perform(put("/api/test/deliveries/" + COURIER_ID + "/status")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("delivery-status-updated-" + COURIER_ID));
        }

        @Test
        @DisplayName("COURIER cannot update other's delivery status")
        void courierCannotUpdateOtherDeliveryStatus() throws Exception {
            mockMvc.perform(put("/api/test/deliveries/" + OTHER_CUSTOMER_ID + "/status")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN can update any delivery status")
        void adminCanUpdateAnyDeliveryStatus() throws Exception {
            mockMvc.perform(put("/api/test/deliveries/" + COURIER_ID + "/status")
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER cannot update delivery status")
        void customerCannotUpdateDeliveryStatus() throws Exception {
            mockMvc.perform(put("/api/test/deliveries/" + CUSTOMER_ID + "/status")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER cannot update delivery status")
        void restaurantOwnerCannotUpdateDeliveryStatus() throws Exception {
            mockMvc.perform(put("/api/test/deliveries/" + OWNER_ID + "/status")
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /couriers/{id}/orders — View Courier's Orders")
    class ViewCourierOrders {

        @Test
        @DisplayName("COURIER can view own assigned orders")
        void courierCanViewOwnOrders() throws Exception {
            mockMvc.perform(get("/api/test/couriers/" + COURIER_ID + "/orders")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("courier-orders-" + COURIER_ID));
        }

        @Test
        @DisplayName("COURIER cannot view other courier's orders")
        void courierCannotViewOtherCourierOrders() throws Exception {
            mockMvc.perform(get("/api/test/couriers/other-courier/orders")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN can view any courier's orders")
        void adminCanViewAnyCourierOrders() throws Exception {
            mockMvc.perform(get("/api/test/couriers/" + COURIER_ID + "/orders")
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CUSTOMER cannot view courier orders")
        void customerCannotViewCourierOrders() throws Exception {
            mockMvc.perform(get("/api/test/couriers/" + COURIER_ID + "/orders")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }
}
