package net.chrisrichardson.ftgo.security.test;

import net.chrisrichardson.ftgo.security.jwt.JwtProperties;
import net.chrisrichardson.ftgo.security.jwt.JwtTokenProvider;
import net.chrisrichardson.ftgo.security.jwt.JwtTokenValidator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Test helper for generating JWT tokens in integration and unit tests.
 *
 * <p>Provides a pre-configured {@link JwtTokenProvider} and {@link JwtTokenValidator}
 * with deterministic test defaults, so tests do not depend on external configuration.
 *
 * <p>Usage in a Spring Boot test:
 * <pre>
 * JwtTestSupport jwt = JwtTestSupport.withDefaults();
 * String token = jwt.createAccessToken("user-123", Arrays.asList("ROLE_USER"));
 *
 * mockMvc.perform(get("/orders")
 *         .header("Authorization", "Bearer " + token))
 *     .andExpect(status().isOk());
 * </pre>
 */
public class JwtTestSupport {

    /** Deterministic test secret (Base64-encoded, 256-bit). */
    public static final String TEST_SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1mdGdvLWp3dC10ZXN0aW5n";

    /** Default test issuer. */
    public static final String TEST_ISSUER = "ftgo-test";

    /** Default test access token expiration: 1 hour. */
    public static final long TEST_EXPIRATION_MS = 3600000L;

    /** Default test refresh token expiration: 24 hours. */
    public static final long TEST_REFRESH_EXPIRATION_MS = 86400000L;

    private final JwtProperties properties;
    private final JwtTokenProvider tokenProvider;
    private final JwtTokenValidator tokenValidator;

    private JwtTestSupport(JwtProperties properties) {
        this.properties = properties;
        this.tokenProvider = new JwtTokenProvider(properties);
        this.tokenValidator = new JwtTokenValidator(properties);
    }

    /**
     * Creates a {@link JwtTestSupport} instance with default test configuration.
     */
    public static JwtTestSupport withDefaults() {
        JwtProperties props = new JwtProperties();
        props.setSecret(TEST_SECRET);
        props.setIssuer(TEST_ISSUER);
        props.setExpirationMs(TEST_EXPIRATION_MS);
        props.setRefreshExpirationMs(TEST_REFRESH_EXPIRATION_MS);
        return new JwtTestSupport(props);
    }

    /**
     * Creates a {@link JwtTestSupport} instance with a custom secret.
     *
     * @param secret Base64-encoded secret key
     */
    public static JwtTestSupport withSecret(String secret) {
        JwtProperties props = new JwtProperties();
        props.setSecret(secret);
        props.setIssuer(TEST_ISSUER);
        props.setExpirationMs(TEST_EXPIRATION_MS);
        props.setRefreshExpirationMs(TEST_REFRESH_EXPIRATION_MS);
        return new JwtTestSupport(props);
    }

    /**
     * Creates an access token for the given user with the specified roles.
     *
     * @param userId the user identifier
     * @param roles  the roles to include in the token
     * @return a compact, signed JWT access token
     */
    public String createAccessToken(String userId, Collection<String> roles) {
        return tokenProvider.createAccessToken(userId, roles, Collections.<String>emptyList());
    }

    /**
     * Creates an access token with roles and permissions.
     *
     * @param userId      the user identifier
     * @param roles       the roles to include
     * @param permissions the permissions to include
     * @return a compact, signed JWT access token
     */
    public String createAccessToken(String userId, Collection<String> roles, Collection<String> permissions) {
        return tokenProvider.createAccessToken(userId, roles, permissions);
    }

    /**
     * Creates a refresh token for the given user.
     *
     * @param userId the user identifier
     * @return a compact, signed JWT refresh token
     */
    public String createRefreshToken(String userId) {
        return tokenProvider.createRefreshToken(userId);
    }

    /**
     * Convenience method to create a token for a user with a single role.
     *
     * @param userId the user identifier
     * @param role   the role (e.g., {@code "ROLE_USER"})
     * @return a compact, signed JWT access token
     */
    public String createAccessTokenWithRole(String userId, String role) {
        return tokenProvider.createAccessToken(userId, Arrays.asList(role), Collections.<String>emptyList());
    }

    /**
     * Returns the {@code Authorization} header value (with Bearer prefix) for the given token.
     *
     * @param token the compact JWT string
     * @return the full header value (e.g., {@code "Bearer eyJ..."})
     */
    public String bearerHeader(String token) {
        return properties.getTokenPrefix() + token;
    }

    /**
     * Returns the underlying {@link JwtTokenProvider}.
     */
    public JwtTokenProvider getTokenProvider() {
        return tokenProvider;
    }

    /**
     * Returns the underlying {@link JwtTokenValidator}.
     */
    public JwtTokenValidator getTokenValidator() {
        return tokenValidator;
    }

    /**
     * Returns the test {@link JwtProperties}.
     */
    public JwtProperties getProperties() {
        return properties;
    }
}
