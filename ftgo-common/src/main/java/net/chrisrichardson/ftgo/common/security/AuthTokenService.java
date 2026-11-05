package net.chrisrichardson.ftgo.common.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

public class AuthTokenService {

  private static final String HMAC_ALGORITHM = "HmacSHA256";
  private final byte[] secret;
  private final Clock clock;

  public AuthTokenService(byte[] secret, Clock clock) {
    if (secret == null || secret.length < 32) {
      throw new IllegalArgumentException("Secret must contain at least 32 bytes");
    }
    if (clock == null) {
      throw new IllegalArgumentException("Clock must not be null");
    }
    this.secret = secret.clone();
    this.clock = clock;
  }

  public AuthTokenService(byte[] secret) {
    this(secret, Clock.systemUTC());
  }

  public static AuthTokenService fromSecret(String secret) {
    if (secret == null) {
      throw new IllegalArgumentException("Secret must contain at least 32 bytes");
    }
    return new AuthTokenService(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String issue(AuthenticatedPrincipal principal, Duration ttl) {
    String payload = principal.getType().name() + ":" + principal.getId() + ":" +
            clock.instant().plus(ttl).getEpochSecond();
    String encodedPayload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    return encodedPayload + "." + Base64.getUrlEncoder().withoutPadding()
            .encodeToString(sign(encodedPayload));
  }

  public Optional<AuthenticatedPrincipal> verify(String token) {
    if (token == null || token.trim().isEmpty()) {
      return Optional.empty();
    }

    try {
      String[] parts = token.split("\\.", -1);
      if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
        return Optional.empty();
      }

      byte[] suppliedSignature = Base64.getUrlDecoder().decode(parts[1]);
      if (!parts[1].equals(Base64.getUrlEncoder().withoutPadding().encodeToString(suppliedSignature)) ||
              !MessageDigest.isEqual(sign(parts[0]), suppliedSignature)) {
        return Optional.empty();
      }

      byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[0]);
      if (!parts[0].equals(Base64.getUrlEncoder().withoutPadding().encodeToString(payloadBytes))) {
        return Optional.empty();
      }
      String[] fields = new String(payloadBytes, StandardCharsets.UTF_8).split(":", -1);
      if (fields.length != 3) {
        return Optional.empty();
      }

      PrincipalType type = PrincipalType.valueOf(fields[0]);
      long id = Long.parseLong(fields[1]);
      long expiration = Long.parseLong(fields[2]);
      if (clock.instant().getEpochSecond() >= expiration) {
        return Optional.empty();
      }

      switch (type) {
        case CONSUMER:
          return Optional.of(AuthenticatedPrincipal.consumer(id));
        case RESTAURANT:
          return Optional.of(AuthenticatedPrincipal.restaurant(id));
        case COURIER:
          return Optional.of(AuthenticatedPrincipal.courier(id));
        default:
          return Optional.empty();
      }
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private byte[] sign(String value) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
      return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new IllegalStateException("Unable to sign authentication token", e);
    }
  }
}
