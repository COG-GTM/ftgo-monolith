package net.chrisrichardson.ftgo.endtoendtests.common;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Mints the JWTs that the order endpoints require. The secret must match the
 * application's ftgo.security.jwt.secret (FTGO_SECURITY_JWT_SECRET).
 */
public class TestAuthTokens {

  public static final String SECRET_ENV_VAR = "FTGO_SECURITY_JWT_SECRET";
  public static final String DEFAULT_TEST_SECRET = "ftgo-test-only-jwt-secret-with-at-least-32-bytes";

  public static String secret() {
    String fromEnv = System.getenv(SECRET_ENV_VAR);
    return fromEnv == null || fromEnv.isEmpty() ? DEFAULT_TEST_SECRET : fromEnv;
  }

  public static String consumerToken(long consumerId) {
    return token("CONSUMER", "consumerId", consumerId);
  }

  public static String restaurantToken(long restaurantId) {
    return token("RESTAURANT", "restaurantId", restaurantId);
  }

  public static String courierToken(long courierId) {
    return token("COURIER", "courierId", courierId);
  }

  private static String token(String role, String idClaim, long id) {
    return Jwts.builder()
            .setSubject(role.toLowerCase() + "-" + id)
            .claim("role", role)
            .claim(idClaim, id)
            .setExpiration(new Date(System.currentTimeMillis() + 3600_000))
            .signWith(SignatureAlgorithm.HS256, new SecretKeySpec(secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
            .compact();
  }
}
