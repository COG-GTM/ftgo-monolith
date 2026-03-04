package com.ftgo.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that the API Gateway application context loads successfully.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FtgoApiGatewayApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the Spring context starts without errors
    }
}
