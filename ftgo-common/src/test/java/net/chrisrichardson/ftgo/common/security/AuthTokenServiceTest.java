package net.chrisrichardson.ftgo.common.security;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthTokenServiceTest {

  private static final byte[] SECRET = "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8);
  private static final Instant NOW = Instant.ofEpochSecond(1_000_000);
  private final AuthTokenService service = new AuthTokenService(SECRET,
          Clock.fixed(NOW, ZoneOffset.UTC));

  @Test
  public void shouldRoundTripEveryPrincipalType() {
    for (AuthenticatedPrincipal principal : Arrays.asList(
            AuthenticatedPrincipal.consumer(12),
            AuthenticatedPrincipal.restaurant(34),
            AuthenticatedPrincipal.courier(56))) {
      assertEquals(Optional.of(principal), service.verify(service.issue(principal, Duration.ofMinutes(5))));
    }
  }

  @Test
  public void shouldRejectTamperedPayloadAndSignature() {
    String token = service.issue(AuthenticatedPrincipal.consumer(12), Duration.ofMinutes(5));
    String[] parts = token.split("\\.");
    String changedPayload = parts[0].substring(0, parts[0].length() - 1) +
            (parts[0].endsWith("A") ? "B" : "A");
    String changedSignature = parts[1].substring(0, parts[1].length() - 1) +
            (parts[1].endsWith("A") ? "B" : "A");

    assertFalse(service.verify(changedPayload + "." + parts[1]).isPresent());
    assertFalse(service.verify(parts[0] + "." + changedSignature).isPresent());
  }

  @Test
  public void shouldRejectTokenSignedByDifferentSecret() {
    AuthTokenService otherService = new AuthTokenService(
            "abcdefghijklmnopqrstuvwxyz123456".getBytes(StandardCharsets.UTF_8),
            Clock.fixed(NOW, ZoneOffset.UTC));
    String token = service.issue(AuthenticatedPrincipal.consumer(12), Duration.ofMinutes(5));

    assertFalse(otherService.verify(token).isPresent());
  }

  @Test
  public void shouldRejectExpiredAndExactlyExpiredTokens() {
    AuthTokenService expiredAtNow = new AuthTokenService(SECRET, Clock.fixed(NOW, ZoneOffset.UTC));
    assertFalse(expiredAtNow.verify(expiredAtNow.issue(
            AuthenticatedPrincipal.consumer(12), Duration.ZERO)).isPresent());

    AuthTokenService issuedEarlier = new AuthTokenService(SECRET,
            Clock.fixed(NOW.minusSeconds(1), ZoneOffset.UTC));
    assertFalse(expiredAtNow.verify(issuedEarlier.issue(
            AuthenticatedPrincipal.consumer(12), Duration.ofSeconds(1))).isPresent());
  }

  @Test
  public void shouldRejectMalformedTokens() {
    for (String token : Arrays.asList(null, "", "abc", "a.b.c", "!.!")) {
      assertFalse(service.verify(token).isPresent());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectShortSecret() {
    new AuthTokenService(new byte[31]);
  }
}
