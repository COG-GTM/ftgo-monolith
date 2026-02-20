package com.ftgo.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class FtgoSecurityConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void securityFilterChainBeanExists() {
        assertThat(applicationContext.getBean(SecurityFilterChain.class)).isNotNull();
    }

    @Test
    void corsConfigurationSourceBeanExists() {
        assertThat(applicationContext.getBean("corsConfigurationSource", CorsConfigurationSource.class)).isNotNull();
    }

    @Test
    void actuatorEndpointsArePublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointsAreDeniedWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    void csrfIsDisabledForPostRequests() throws Exception {
        mockMvc.perform(post("/api/test"))
                .andExpect(status().isForbidden());
    }
}
