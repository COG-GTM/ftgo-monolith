package net.chrisrichardson.ftgo.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that the Spring application context loads successfully.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FtgoApiGatewayApplicationTest {

    @Test
    void contextLoads() {
        // Verifies the application context starts without errors
    }
}
