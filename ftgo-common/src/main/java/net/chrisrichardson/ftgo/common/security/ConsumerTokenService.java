package net.chrisrichardson.ftgo.common.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

public class ConsumerTokenService {

  private static final Logger logger = LoggerFactory.getLogger(ConsumerTokenService.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder decoder = Base64.getUrlDecoder();

  private final byte[] secret;
  private final Clock clock;

  public ConsumerTokenService(String secret) {
    this(secret, Clock.systemUTC());
  }

  public ConsumerTokenService(String secret, Clock clock) {
    this.clock = clock;
    if (secret == null || secret.trim().isEmpty()) {
      this.secret = null;
      logger.warn("Order read endpoints will reject all requests until FTGO_CONSUMER_TOKEN_SECRET is set");
    } else {
      byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
      if (secretBytes.length < 32) {
        throw new IllegalArgumentException("Consumer token secret must be at least 32 UTF-8 bytes");
      }
      this.secret = secretBytes;
    }
  }

  public Optional<AuthenticatedConsumer> verify(String token) {
    if (secret == null || token == null) {
      return Optional.empty();
    }

    try {
      String[] parts = token.split("\\.", -1);
      if (parts.length != 3 || !isBase64Url(parts[0]) || !isBase64Url(parts[1]) || !isBase64Url(parts[2])) {
        return Optional.empty();
      }

      JsonNode header = parseJson(decoder.decode(parts[0]));
      if (header == null || !header.isObject() || !header.has("alg")
              || !header.get("alg").isTextual() || !"HS256".equals(header.get("alg").textValue())) {
        return Optional.empty();
      }

      byte[] signature = decoder.decode(parts[2]);
      byte[] expectedSignature = sign(parts[0] + "." + parts[1]);
      if (!MessageDigest.isEqual(expectedSignature, signature)) {
        return Optional.empty();
      }

      JsonNode payload = parseJson(decoder.decode(parts[1]));
      if (payload == null || !payload.isObject()) {
        return Optional.empty();
      }

      JsonNode subject = payload.get("sub");
      if (subject == null || !subject.isTextual()) {
        return Optional.empty();
      }
      long consumerId = Long.parseLong(subject.textValue());
      if (consumerId <= 0) {
        return Optional.empty();
      }

      long now = clock.instant().getEpochSecond();
      JsonNode expiration = payload.get("exp");
      if (!isEpochSeconds(expiration) || expiration.longValue() <= now) {
        return Optional.empty();
      }

      JsonNode notBefore = payload.get("nbf");
      if (notBefore != null && (!isEpochSeconds(notBefore) || notBefore.longValue() > now)) {
        return Optional.empty();
      }

      return Optional.of(new AuthenticatedConsumer(consumerId));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public String issue(long consumerId, Duration ttl) {
    if (secret == null) {
      throw new IllegalStateException("Consumer token service is disabled");
    }

    Instant now = clock.instant();
    ObjectNode header = objectMapper.createObjectNode();
    header.put("alg", "HS256");
    header.put("typ", "JWT");
    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("sub", Long.toString(consumerId));
    payload.put("iat", now.getEpochSecond());
    payload.put("exp", now.plus(ttl).getEpochSecond());

    try {
      String headerPart = encoder.encodeToString(objectMapper.writeValueAsBytes(header));
      String payloadPart = encoder.encodeToString(objectMapper.writeValueAsBytes(payload));
      String unsignedToken = headerPart + "." + payloadPart;
      return unsignedToken + "." + encoder.encodeToString(sign(unsignedToken));
    } catch (JsonProcessingException | GeneralSecurityException e) {
      throw new IllegalStateException("Unable to issue consumer token", e);
    }
  }

  private byte[] sign(String value) throws GeneralSecurityException {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret, "HmacSHA256"));
    return mac.doFinal(value.getBytes(StandardCharsets.US_ASCII));
  }

  private boolean isBase64Url(String value) {
    return value.matches("[A-Za-z0-9_-]*");
  }

  private JsonNode parseJson(byte[] value) throws IOException {
    try (JsonParser parser = objectMapper.getFactory().createParser(value)) {
      JsonNode result = objectMapper.readTree(parser);
      return parser.nextToken() == null ? result : null;
    }
  }

  private boolean isEpochSeconds(JsonNode value) {
    return value != null && value.isIntegralNumber() && value.canConvertToLong();
  }
}
