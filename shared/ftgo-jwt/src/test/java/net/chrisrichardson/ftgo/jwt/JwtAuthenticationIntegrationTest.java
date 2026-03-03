package net.chrisrichardson.ftgo.jwt;

import net.chrisrichardson.ftgo.jwt.config.FtgoJwtSecurityConfig;
import net.chrisrichardson.ftgo.jwt.model.FtgoUserContext;
import net.chrisrichardson.ftgo.jwt.service.JwtTokenService;
import net.chrisrichardson.ftgo.jwt.util.JwtUserContextHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for JWT authentication flow.
 *
 * <p>These tests verify the complete JWT authentication pipeline including:</p>
 * <ul>
 *   <li>Valid JWT tokens are accepted and user context is populated</li>
 *   <li>Expired tokens return 401 Unauthorized</li>
 *   <li>Invalid/tampered tokens return 401 Unauthorized</li>
 *   <li>Missing tokens return 401 Unauthorized</li>
 *   <li>Excluded paths (e.g., health checks) are accessible without tokens</li>
 *   <li>User context is available in the service layer via SecurityContextHolder</li>
 *   <li>Roles and permissions are correctly propagated</li>
 * </ul>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = JwtAuthenticationIntegrationTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "ftgo.jwt.secret=integration-test-secret-key-that-is-at-least-32-bytes-long!!",
                "ftgo.jwt.issuer=https://test-issuer.ftgo.com/realms/ftgo",
                "ftgo.jwt.expiration-seconds=3600",
                "ftgo.jwt.refresh-expiration-seconds=86400",
                "spring.autoconfigure.exclude=net.chrisrichardson.ftgo.security.FtgoSecurityAutoConfiguration"
        }
)
@AutoConfigureMockMvc
public class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    // -------------------------------------------------------------------------
    // Valid Token Tests
    // -------------------------------------------------------------------------

    @Test
    public void shouldAcceptValidJwtToken() throws Exception {
        FtgoUserContext userContext = FtgoUserContext.builder()
                .userId("user-001")
                .username("john.doe")
                .roles(Arrays.asList("USER"))
                .permissions(Arrays.asList("order:read"))
                .build();

        String token = jwtTokenService.generateAccessToken(userContext);

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    public void shouldPopulateUserContextFromJwtToken() throws Exception {
        FtgoUserContext userContext = FtgoUserContext.builder()
                .userId("user-context-test")
                .username("context.user")
                .roles(Arrays.asList("ADMIN", "USER"))
                .permissions(Arrays.asList("order:read", "order:write"))
                .build();

        String token = jwtTokenService.generateAccessToken(userContext);

        mockMvc.perform(get("/api/user-info")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-context-test"))
                .andExpect(jsonPath("$.username").value("context.user"));
    }

    @Test
    public void shouldMakeRolesAvailableInSecurityContext() throws Exception {
        FtgoUserContext userContext = FtgoUserContext.builder()
                .userId("admin-user")
                .username("admin")
                .roles(Arrays.asList("ADMIN"))
                .permissions(Collections.<String>emptyList())
                .build();

        String token = jwtTokenService.generateAccessToken(userContext);

        mockMvc.perform(get("/api/admin-check")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // -------------------------------------------------------------------------
    // Invalid Token Tests
    // -------------------------------------------------------------------------

    @Test
    public void shouldReturn401ForMissingToken() throws Exception {
        mockMvc.perform(get("/api/test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturn401ForMissingBearerPrefix() throws Exception {
        FtgoUserContext userContext = FtgoUserContext.builder()
                .userId("user-001")
                .username("john.doe")
                .roles(Arrays.asList("USER"))
                .permissions(Collections.<String>emptyList())
                .build();

        String token = jwtTokenService.generateAccessToken(userContext);

        mockMvc.perform(get("/api/test")
                        .header("Authorization", token) // missing "Bearer " prefix
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturn401ForMalformedToken() throws Exception {
        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer not.a.valid.jwt")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturn401ForTamperedToken() throws Exception {
        FtgoUserContext userContext = FtgoUserContext.builder()
                .userId("user-001")
                .username("john.doe")
                .roles(Arrays.asList("USER"))
                .permissions(Collections.<String>emptyList())
                .build();

        String token = jwtTokenService.generateAccessToken(userContext);
        String tamperedToken = token.substring(0, token.length() - 1) +
                (token.endsWith("A") ? "B" : "A");

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + tamperedToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturn401ForTokenWithWrongSigningKey() throws Exception {
        // Create a token with a different signing key
        FtgoJwtProperties differentKeyProperties = new FtgoJwtProperties();
        differentKeyProperties.setSecret("a-completely-different-secret-key-at-least-32-bytes!!");
        differentKeyProperties.setIssuer("https://test-issuer.ftgo.com/realms/ftgo");
        JwtTokenService differentKeyService = new JwtTokenService(differentKeyProperties);

        FtgoUserContext userContext = FtgoUserContext.builder()
                .userId("user-001")
                .username("john.doe")
                .roles(Arrays.asList("USER"))
                .permissions(Collections.<String>emptyList())
                .build();

        String wrongKeyToken = differentKeyService.generateAccessToken(userContext);

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + wrongKeyToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturn401ForExpiredToken() throws Exception {
        // Build a token that expired 5 minutes ago (well beyond 30s clock skew tolerance)
        // using the same signing key as the server
        javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                "integration-test-secret-key-that-is-at-least-32-bytes-long!!"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        java.util.Date fiveMinutesAgo = new java.util.Date(System.currentTimeMillis() - 300_000);
        java.util.Date tenMinutesAgo = new java.util.Date(System.currentTimeMillis() - 600_000);

        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .setSubject("user-001")
                .setIssuer("https://test-issuer.ftgo.com/realms/ftgo")
                .setIssuedAt(tenMinutesAgo)
                .setExpiration(fiveMinutesAgo)
                .claim("username", "john.doe")
                .claim("roles", Arrays.asList("USER"))
                .claim("permissions", Collections.<String>emptyList())
                .claim("token_type", "access")
                .signWith(key, io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + expiredToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // Excluded Path Tests
    // -------------------------------------------------------------------------

    @Test
    public void shouldAllowAccessToHealthEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldAllowAccessToInfoEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/info")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // Test Configuration
    // -------------------------------------------------------------------------

    @Configuration
    @EnableConfigurationProperties(FtgoJwtProperties.class)
    @Import({
            FtgoJwtSecurityConfig.class,
            TestController.class,
            // Spring Boot auto-configurations needed for testing
            DispatcherServletAutoConfiguration.class,
            WebMvcAutoConfiguration.class,
            HttpMessageConvertersAutoConfiguration.class,
            JacksonAutoConfiguration.class,
    })
    static class TestConfig {

        @Bean
        public JwtTokenService jwtTokenService(FtgoJwtProperties jwtProperties) {
            return new JwtTokenService(jwtProperties);
        }
    }

    /**
     * Test controller that simulates real service endpoints and health/info endpoints.
     */
    @RestController
    static class TestController {

        @GetMapping("/api/test")
        public String test() {
            return "OK";
        }

        @GetMapping("/api/user-info")
        public java.util.Map<String, Object> userInfo() {
            java.util.Map<String, Object> info = new java.util.LinkedHashMap<>();
            JwtUserContextHolder.getCurrentUser().ifPresent(user -> {
                info.put("userId", user.getUserId());
                info.put("username", user.getUsername());
                info.put("roles", user.getRoles());
                info.put("permissions", user.getPermissions());
            });
            return info;
        }

        @GetMapping("/api/admin-check")
        public String adminCheck() {
            return String.valueOf(
                    JwtUserContextHolder.getCurrentUser()
                            .map(user -> user.hasRole("ADMIN"))
                            .orElse(false));
        }

        @GetMapping("/actuator/health")
        public String health() {
            return "{\"status\":\"UP\"}";
        }

        @GetMapping("/actuator/info")
        public String info() {
            return "{}";
        }
    }
}
