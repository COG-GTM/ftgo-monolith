package com.ftgo.template.api;

// =============================================================================
// TEMPLATE: API Integration Test (Full Stack with Testcontainers)
// =============================================================================
// Copy this file when creating a new microservice.
// Replace "template" with your service name and "Example" with your resource.
//
// This template demonstrates:
//   - Full Spring Boot context with a real database
//   - TestRestTemplate for HTTP testing (or MockMvc)
//   - @SpringBootTest with RANDOM_PORT
//   - Testing the complete request -> controller -> service -> repository flow
//   - Test data setup and cleanup
//   - Security integration (JWT token in requests)
//
// When to use API integration tests vs. controller unit tests:
//   - Controller unit tests: Fast, mock service layer, test request/response mapping
//   - API integration tests: Slower, real dependencies, test complete flow
//
// Use API integration tests sparingly for critical paths only.
// =============================================================================

import com.ftgo.template.config.AbstractMySqlIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API integration tests that validate the complete request/response flow.
 *
 * <p>These tests start the full Spring Boot application on a random port with a
 * real MySQL database via Testcontainers. They test the complete stack from HTTP
 * request through to database persistence.
 *
 * <p><b>Use sparingly:</b> Only for critical business flows. Prefer unit tests
 * for edge cases and error handling.
 *
 * <p><b>Location:</b> {@code src/integration-test/java/com/ftgo/{service}/api/}
 */
// TODO: Uncomment when you have a Spring Boot application class
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @ActiveProfiles("integration-test")
@DisplayName("Example API Integration Tests")
class ExampleApiIntegrationTest extends AbstractMySqlIntegrationTest {

    // @Autowired
    // private TestRestTemplate restTemplate;

    // For JWT-protected endpoints:
    // @Autowired
    // private JwtTokenService jwtTokenService;

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Creates HTTP headers with a valid JWT token for authenticated requests.
     */
    private HttpHeaders authenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // var tokens = jwtTokenService.issueTokens(1L, "testuser",
        //     List.of("ROLE_USER"), List.of());
        // headers.setBearerAuth(tokens.getAccessToken());
        return headers;
    }

    // -------------------------------------------------------------------------
    // Full-stack API tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/examples -> GET /api/v1/examples/{id}")
    class CreateAndRetrieve {

        @Test
        @DisplayName("should create entity via POST and retrieve via GET")
        void shouldCreateAndRetrieveEntity() {
            // Step 1: Create entity via POST
            // var createRequest = new CreateExampleRequest("Integration Test Entity", "Description");
            // var postResponse = restTemplate.exchange(
            //     "/api/v1/examples",
            //     HttpMethod.POST,
            //     new HttpEntity<>(createRequest, authenticatedHeaders()),
            //     ApiResponse.class);
            //
            // assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            // Long createdId = extractIdFromResponse(postResponse.getBody());

            // Step 2: Retrieve entity via GET
            // var getResponse = restTemplate.exchange(
            //     "/api/v1/examples/" + createdId,
            //     HttpMethod.GET,
            //     new HttpEntity<>(authenticatedHeaders()),
            //     ApiResponse.class);
            //
            // assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            // assertThat(getResponse.getBody().getData().get("name"))
            //     .isEqualTo("Integration Test Entity");
        }
    }

    @Nested
    @DisplayName("Full lifecycle")
    class FullLifecycle {

        @Test
        @DisplayName("should support full CRUD lifecycle")
        void shouldSupportFullLifecycle() {
            // 1. CREATE
            // var createRequest = new CreateExampleRequest("Lifecycle Entity", "Desc");
            // var created = restTemplate.exchange("/api/v1/examples",
            //     HttpMethod.POST, new HttpEntity<>(createRequest, authenticatedHeaders()),
            //     ApiResponse.class);
            // assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            // 2. READ
            // Long id = extractIdFromResponse(created.getBody());
            // var read = restTemplate.exchange("/api/v1/examples/" + id,
            //     HttpMethod.GET, new HttpEntity<>(authenticatedHeaders()),
            //     ApiResponse.class);
            // assertThat(read.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 3. UPDATE
            // var updateRequest = new UpdateExampleRequest("Updated Name", "Updated Desc");
            // var updated = restTemplate.exchange("/api/v1/examples/" + id,
            //     HttpMethod.PUT, new HttpEntity<>(updateRequest, authenticatedHeaders()),
            //     ApiResponse.class);
            // assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 4. DELETE
            // var deleted = restTemplate.exchange("/api/v1/examples/" + id,
            //     HttpMethod.DELETE, new HttpEntity<>(authenticatedHeaders()),
            //     Void.class);
            // assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // 5. VERIFY DELETED
            // var notFound = restTemplate.exchange("/api/v1/examples/" + id,
            //     HttpMethod.GET, new HttpEntity<>(authenticatedHeaders()),
            //     ApiResponse.class);
            // assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // -------------------------------------------------------------------------
    // Security integration tests
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Security")
    class Security {

        @Test
        @DisplayName("should reject unauthenticated requests")
        void shouldRejectUnauthenticated() {
            // var response = restTemplate.getForEntity("/api/v1/examples", String.class);
            // assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("should accept requests with valid JWT")
        void shouldAcceptAuthenticatedRequests() {
            // var response = restTemplate.exchange("/api/v1/examples",
            //     HttpMethod.GET, new HttpEntity<>(authenticatedHeaders()),
            //     String.class);
            // assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
