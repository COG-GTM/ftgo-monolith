package net.chrisrichardson.ftgo.orderservice.web.security;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class TestJwtTokens {

  public static final String TEST_SECRET = "test-only-jwt-secret-that-is-at-least-32-bytes-long";
  public static final String OTHER_SECRET = "another-test-only-secret-that-is-32-bytes-or-more";

  public static String token(FtgoRole role, long id) {
    return token(TEST_SECRET, role, id, new Date(System.currentTimeMillis() + 60_000));
  }

  public static String token(String secret, FtgoRole role, long id, Date expiration) {
    return builder(secret, role, id, expiration).compact();
  }

  public static JwtBuilder builder(String secret, FtgoRole role, long id, Date expiration) {
    JwtBuilder builder = Jwts.builder()
            .setSubject(role.name().toLowerCase() + "-" + id)
            .claim(JwtTokenVerifier.ROLE_CLAIM, role.name())
            .claim(role.getIdClaim(), id)
            .signWith(SignatureAlgorithm.HS256, new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    if (expiration != null) {
      builder.setExpiration(expiration);
    }
    return builder;
  }
}
