package com.ftgo.security.authorization;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RbacTestApplication.class)
@AutoConfigureMockMvc
class MethodSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedAccessToDeniedEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/config"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/config"))
                .andExpect(status().isOk())
                .andExpect(content().string("admin-config"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/config"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotAccessAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/config"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessManagerEndpoint() throws Exception {
        mockMvc.perform(get("/api/manager/reports"))
                .andExpect(status().isOk())
                .andExpect(content().string("manager-reports"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerCanAccessManagerEndpoint() throws Exception {
        mockMvc.perform(get("/api/manager/reports"))
                .andExpect(status().isOk())
                .andExpect(content().string("manager-reports"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotAccessManagerEndpoint() throws Exception {
        mockMvc.perform(get("/api/manager/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCanAccessUserEndpoint() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().string("orders-list"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessUserEndpoint() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().string("orders-list"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerCanAccessUserEndpoint() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().string("orders-list"));
    }

    @Test
    @WithMockUser(roles = "SERVICE")
    void serviceCanAccessServiceEndpoint() throws Exception {
        mockMvc.perform(get("/api/internal/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("service-health"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotAccessServiceEndpoint() throws Exception {
        mockMvc.perform(get("/api/internal/health"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAdminOrServiceEndpoint() throws Exception {
        mockMvc.perform(post("/api/orders/sync"))
                .andExpect(status().isOk())
                .andExpect(content().string("orders-synced"));
    }

    @Test
    @WithMockUser(roles = "SERVICE")
    void serviceCanAccessAdminOrServiceEndpoint() throws Exception {
        mockMvc.perform(post("/api/orders/sync"))
                .andExpect(status().isOk())
                .andExpect(content().string("orders-synced"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotAccessAdminOrServiceEndpoint() throws Exception {
        mockMvc.perform(post("/api/orders/sync"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDeleteUsers() throws Exception {
        mockMvc.perform(delete("/api/users/123"))
                .andExpect(status().isOk())
                .andExpect(content().string("user-deleted"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerCannotDeleteUsers() throws Exception {
        mockMvc.perform(delete("/api/users/123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerCanUpdateRestaurant() throws Exception {
        mockMvc.perform(put("/api/restaurants/456"))
                .andExpect(status().isOk())
                .andExpect(content().string("restaurant-updated"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotUpdateRestaurant() throws Exception {
        mockMvc.perform(put("/api/restaurants/456"))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicEndpointAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("UP"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void securedAnnotationWorksForAdmin() throws Exception {
        mockMvc.perform(get("/api/secured/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("secured-admin"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void securedAnnotationDeniesUser() throws Exception {
        mockMvc.perform(get("/api/secured/admin"))
                .andExpect(status().isForbidden());
    }
}
