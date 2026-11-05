package net.chrisrichardson.ftgo.common.security;

import org.junit.Before;
import org.junit.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ConsumerTokenServiceTest {

  private static final String SECRET = "0123456789abcdef0123456789abcdef";
  private static final Instant NOW = Instant.parse("2024-01-01T00:00:00Z");
  private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

  private ConsumerTokenService service;

  @Before
  public void setUp() {
    service = new ConsumerTokenService(SECRET, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  public void validTokenRoundTrips() {
    long consumerId = 1511300065921L;

    assertEquals(consumerId, service.verify(service.issue(consumerId, Duration.ofMinutes(30))).get().getConsumerId());
  }

  @Test
  public void expiredTokenIsRejected() {
    ConsumerTokenService expiredService = new ConsumerTokenService(SECRET,
            Clock.fixed(NOW.plusSeconds(3600), ZoneOffset.UTC));

    assertFalse(expiredService.verify(service.issue(42, Duration.ofMinutes(30))).isPresent());
  }

  @Test
  public void futureNotBeforeIsRejected() throws Exception {
    String token = signedToken("{\"alg\":\"HS256\",\"typ\":\"JWT\"}",
            "{\"sub\":\"42\",\"exp\":1704069000,\"nbf\":1704067201}", SECRET);

    assertFalse(service.verify(token).isPresent());
  }

  @Test
  public void wrongSecretIsRejected() {
    ConsumerTokenService otherService = new ConsumerTokenService("abcdef0123456789abcdef0123456789",
            Clock.fixed(NOW, ZoneOffset.UTC));

    assertFalse(otherService.verify(service.issue(42, Duration.ofMinutes(30))).isPresent());
  }

  @Test
  public void changedPayloadWithOriginalSignatureIsRejected() {
    String token = service.issue(42, Duration.ofMinutes(30));
    String[] parts = token.split("\\.");
    String changedPayload = encoder.encodeToString("{\"sub\":\"43\",\"iat\":1704067200,\"exp\":1704069000}"
            .getBytes(StandardCharsets.UTF_8));

    assertFalse(service.verify(parts[0] + "." + changedPayload + "." + parts[2]).isPresent());
  }

  @Test
  public void noneAlgorithmIsRejected() throws Exception {
    String token = encoder.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8)) + "."
            + encoder.encodeToString("{\"sub\":\"42\",\"exp\":1704069000}".getBytes(StandardCharsets.UTF_8)) + ".";

    assertFalse(service.verify(token).isPresent());
  }

  @Test
  public void hs512AlgorithmIsRejectedEvenWhenSignedWithHs256() throws Exception {
    String token = signedToken("{\"alg\":\"HS512\",\"typ\":\"JWT\"}",
            "{\"sub\":\"42\",\"exp\":1704069000}", SECRET);

    assertFalse(service.verify(token).isPresent());
  }

  @Test
  public void missingAlgorithmIsRejected() throws Exception {
    String token = signedToken("{\"typ\":\"JWT\"}",
            "{\"sub\":\"42\",\"exp\":1704069000}", SECRET);

    assertFalse(service.verify(token).isPresent());
  }

  @Test
  public void nonStringOrNegativeSubjectIsRejected() throws Exception {
    assertFalse(service.verify(signedToken("{\"alg\":\"HS256\"}",
            "{\"sub\":42,\"exp\":1704069000}", SECRET)).isPresent());
    assertFalse(service.verify(signedToken("{\"alg\":\"HS256\"}",
            "{\"sub\":\"not-a-number\",\"exp\":1704069000}", SECRET)).isPresent());
    assertFalse(service.verify(signedToken("{\"alg\":\"HS256\"}",
            "{\"sub\":\"-42\",\"exp\":1704069000}", SECRET)).isPresent());
  }

  @Test
  public void missingExpirationIsRejected() throws Exception {
    assertFalse(service.verify(signedToken("{\"alg\":\"HS256\"}",
            "{\"sub\":\"42\"}", SECRET)).isPresent());
  }

  @Test
  public void malformedTokensAreRejected() {
    assertFalse(service.verify("garbage").isPresent());
    assertFalse(service.verify("one.two").isPresent());
    assertFalse(service.verify(null).isPresent());
  }

  @Test
  public void disabledServiceRejectsVerificationAndCannotIssue() {
    ConsumerTokenService disabledService = new ConsumerTokenService(" ", Clock.fixed(NOW, ZoneOffset.UTC));

    assertFalse(disabledService.verify(service.issue(42, Duration.ofMinutes(30))).isPresent());
    try {
      disabledService.issue(42, Duration.ofMinutes(30));
    } catch (IllegalStateException expected) {
      return;
    }
    throw new AssertionError("Expected issue to reject disabled service");
  }

  @Test(expected = IllegalArgumentException.class)
  public void secretMustHaveAtLeast32Utf8Bytes() {
    new ConsumerTokenService("short", Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private String signedToken(String header, String payload, String secret) throws Exception {
    String headerPart = encoder.encodeToString(header.getBytes(StandardCharsets.UTF_8));
    String payloadPart = encoder.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    String unsignedToken = headerPart + "." + payloadPart;
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    String signature = encoder.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.US_ASCII)));
    return unsignedToken + "." + signature;
  }
}
