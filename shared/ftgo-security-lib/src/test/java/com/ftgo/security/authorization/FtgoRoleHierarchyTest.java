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

import java.util.Arrays;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the FTGO role hierarchy.
 *
 * <p>Verifies that:
 * <ul>
 *   <li>ADMIN inherits RESTAURANT_OWNER, CUSTOMER, and COURIER permissions</li>
 *   <li>RESTAURANT_OWNER inherits CUSTOMER permissions</li>
 *   <li>COURIER does NOT inherit CUSTOMER or RESTAURANT_OWNER permissions</li>
 *   <li>CUSTOMER does NOT inherit COURIER or RESTAURANT_OWNER permissions</li>
 * </ul>
 */
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class FtgoRoleHierarchyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String restaurantOwnerToken;
    private String customerToken;
    private String courierToken;

    @BeforeEach
    void setUp() {
        adminToken = jwtTokenProvider.generateAccessToken(
                "admin-1", "admin", Collections.singletonList("ROLE_ADMIN"));
        restaurantOwnerToken = jwtTokenProvider.generateAccessToken(
                "owner-1", "owner", Collections.singletonList("ROLE_RESTAURANT_OWNER"));
        customerToken = jwtTokenProvider.generateAccessToken(
                "customer-1", "customer", Collections.singletonList("ROLE_CUSTOMER"));
        courierToken = jwtTokenProvider.generateAccessToken(
                "courier-1", "courier", Collections.singletonList("ROLE_COURIER"));
    }

    @Nested
    @DisplayName("ADMIN role hierarchy")
    class AdminHierarchy {

        @Test
        @DisplayName("ADMIN can access admin-only endpoints")
        void adminCanAccessAdminEndpoints() throws Exception {
            mockMvc.perform(get("/api/test/admin-only")
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("admin-access"));
        }

        @Test
        @DisplayName("ADMIN inherits RESTAURANT_OWNER permissions")
        void adminInheritsRestaurantOwnerPermissions() throws Exception {
            mockMvc.perform(get("/api/test/restaurant-owner-area")
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("restaurant-owner-access"));
        }

        @Test
        @DisplayName("ADMIN inherits CUSTOMER permissions")
        void adminInheritsCustomerPermissions() throws Exception {
            mockMvc.perform(get("/api/test/customer-area")
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("customer-access"));
        }

        @Test
        @DisplayName("ADMIN inherits COURIER permissions")
        void adminInheritsCourierPermissions() throws Exception {
            mockMvc.perform(get("/api/test/courier-area")
                            .header("Authorization", "Bearer " + adminToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("courier-access"));
        }
    }

    @Nested
    @DisplayName("RESTAURANT_OWNER role hierarchy")
    class RestaurantOwnerHierarchy {

        @Test
        @DisplayName("RESTAURANT_OWNER inherits CUSTOMER permissions")
        void restaurantOwnerInheritsCustomerPermissions() throws Exception {
            mockMvc.perform(get("/api/test/customer-area")
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("customer-access"));
        }

        @Test
        @DisplayName("RESTAURANT_OWNER can access own area")
        void restaurantOwnerCanAccessOwnArea() throws Exception {
            mockMvc.perform(get("/api/test/restaurant-owner-area")
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("restaurant-owner-access"));
        }

        @Test
        @DisplayName("RESTAURANT_OWNER cannot access admin-only endpoints")
        void restaurantOwnerCannotAccessAdminEndpoints() throws Exception {
            mockMvc.perform(get("/api/test/admin-only")
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("RESTAURANT_OWNER cannot access courier endpoints")
        void restaurantOwnerCannotAccessCourierEndpoints() throws Exception {
            mockMvc.perform(get("/api/test/courier-area")
                            .header("Authorization", "Bearer " + restaurantOwnerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("CUSTOMER role hierarchy")
    class CustomerHierarchy {

        @Test
        @DisplayName("CUSTOMER can access customer area")
        void customerCanAccessCustomerArea() throws Exception {
            mockMvc.perform(get("/api/test/customer-area")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("customer-access"));
        }

        @Test
        @DisplayName("CUSTOMER cannot access admin-only endpoints")
        void customerCannotAccessAdminEndpoints() throws Exception {
            mockMvc.perform(get("/api/test/admin-only")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("CUSTOMER cannot access restaurant-owner endpoints")
        void customerCannotAccessRestaurantOwnerEndpoints() throws Exception {
            mockMvc.perform(get("/api/test/restaurant-owner-area")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("CUSTOMER cannot access courier endpoints")
        void customerCannotAccessCourierEndpoints() throws Exception {
            mockMvc.perform(get("/api/test/courier-area")
                            .header("Authorization", "Bearer " + customerToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("COURIER role hierarchy")
    class CourierHierarchy {

        @Test
        @DisplayName("COURIER can access courier area")
        void courierCanAccessCourierArea() throws Exception {
            mockMvc.perform(get("/api/test/courier-area")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("courier-access"));
        }

        @Test
        @DisplayName("COURIER cannot access admin-only endpoints")
        void courierCannotAccessAdminEndpoints() throws Exception {
            mockMvc.perform(get("/api/test/admin-only")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("COURIER cannot access customer endpoints")
        void courierCannotAccessCustomerEndpoints() throws Exception {
            mockMvc.perform(get("/api/test/customer-area")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("COURIER cannot access restaurant-owner endpoints")
        void courierCannotAccessRestaurantOwnerEndpoints() throws Exception {
            mockMvc.perform(get("/api/test/restaurant-owner-area")
                            .header("Authorization", "Bearer " + courierToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Unauthenticated access")
    class UnauthenticatedAccess {

        @Test
        @DisplayName("Unauthenticated user gets 401 on protected endpoints")
        void unauthenticatedUserGets401() throws Exception {
            mockMvc.perform(get("/api/test/admin-only")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Unauthenticated user gets 401 on customer area")
        void unauthenticatedUserGets401OnCustomerArea() throws Exception {
            mockMvc.perform(get("/api/test/customer-area")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
