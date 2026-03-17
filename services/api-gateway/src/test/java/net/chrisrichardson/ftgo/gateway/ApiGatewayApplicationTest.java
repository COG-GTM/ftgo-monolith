package net.chrisrichardson.ftgo.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "gateway.jwt.enabled=false",
                "spring.cloud.gateway.routes[0].id=test-route",
                "spring.cloud.gateway.routes[0].uri=http://localhost:8080",
                "spring.cloud.gateway.routes[0].predicates[0]=Path=/test/**",
                "spring.data.redis.host=localhost",
                "spring.data.redis.port=6379",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration"
        }
)
@ActiveProfiles("test")
class ApiGatewayApplicationTest {

    @Test
    void contextLoads() {
        // Verifies the application context starts successfully
    }
}
