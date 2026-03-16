package net.chrisrichardson.ftgo.security.jwt;

import net.chrisrichardson.ftgo.security.test.JwtTestSupport;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtRefreshServiceTest {

    private final JwtTestSupport jwt = JwtTestSupport.withDefaults();
    private final JwtRefreshService refreshService =
            new JwtRefreshService(jwt.getTokenProvider(), jwt.getTokenValidator());

    @Test
    void refresh_validRefreshToken_returnsNewTokenPair() {
        String refreshToken = jwt.createRefreshToken("user-100");

        JwtRefreshService.TokenPair result = refreshService.refresh(
                refreshToken,
                Arrays.asList("ROLE_USER"),
                Collections.singletonList("order:read"));

        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());

        // Verify the new access token is valid
        assertNotNull(jwt.getTokenValidator().validateAndExtractClaims(result.getAccessToken()));
        assertNotNull(jwt.getTokenValidator().validateAndExtractClaims(result.getRefreshToken()));
    }

    @Test
    void refresh_accessTokenInsteadOfRefresh_returnsNull() {
        String accessToken = jwt.createAccessToken("user-200",
                Collections.singletonList("ROLE_USER"));

        JwtRefreshService.TokenPair result = refreshService.refresh(accessToken);
        assertNull(result);
    }

    @Test
    void refresh_invalidToken_returnsNull() {
        JwtRefreshService.TokenPair result = refreshService.refresh("invalid-token");
        assertNull(result);
    }

    @Test
    void refresh_defaultOverload_returnsTokenPair() {
        String refreshToken = jwt.createRefreshToken("user-300");

        JwtRefreshService.TokenPair result = refreshService.refresh(refreshToken);
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
    }
}
