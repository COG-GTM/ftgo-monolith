package net.chrisrichardson.ftgo.orderservice.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class JwtTokenVerifier {

  public static final String ROLE_CLAIM = "role";
  private static final int MIN_SECRET_BYTES = 32;

  private static final Logger logger = LoggerFactory.getLogger(JwtTokenVerifier.class);

  private final SecretKey key;

  public JwtTokenVerifier(String configuredSecret) {
    if (configuredSecret == null || configuredSecret.isEmpty()) {
      logger.warn("ftgo.security.jwt.secret is not configured; using an ephemeral key. "
              + "Set FTGO_SECURITY_JWT_SECRET to accept externally issued tokens.");
      byte[] bytes = new byte[MIN_SECRET_BYTES];
      new SecureRandom().nextBytes(bytes);
      this.key = new SecretKeySpec(bytes, "HmacSHA256");
    } else {
      byte[] bytes = configuredSecret.getBytes(StandardCharsets.UTF_8);
      if (bytes.length < MIN_SECRET_BYTES) {
        throw new IllegalStateException("ftgo.security.jwt.secret must be at least " + MIN_SECRET_BYTES + " bytes");
      }
      this.key = new SecretKeySpec(bytes, "HmacSHA256");
    }
  }

  public FtgoPrincipal verify(String token) {
    Claims claims;
    try {
      claims = Jwts.parser()
              .setSigningKey(key)
              .parseClaimsJws(token)
              .getBody();
    } catch (JwtException | IllegalArgumentException e) {
      throw new BadCredentialsException("Invalid token", e);
    }

    if (claims.getExpiration() == null) {
      throw new BadCredentialsException("Token has no expiration");
    }

    String roleName = claims.get(ROLE_CLAIM, String.class);
    FtgoRole role;
    try {
      role = FtgoRole.valueOf(roleName);
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new BadCredentialsException("Token has no valid role");
    }

    Object rawId = claims.get(role.getIdClaim());
    if (!(rawId instanceof Number)) {
      throw new BadCredentialsException("Token has no " + role.getIdClaim());
    }

    String subject = claims.getSubject() == null ? role.name().toLowerCase() + "-" + rawId : claims.getSubject();
    return new FtgoPrincipal(subject, role, ((Number) rawId).longValue());
  }
}
