package net.chrisrichardson.ftgo.orderservice.web.security;

import io.jsonwebtoken.Jwts;
import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Date;

import static net.chrisrichardson.ftgo.orderservice.web.security.TestJwtTokens.OTHER_SECRET;
import static net.chrisrichardson.ftgo.orderservice.web.security.TestJwtTokens.TEST_SECRET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JwtTokenVerifierTest {

  private final JwtTokenVerifier verifier = new JwtTokenVerifier(TEST_SECRET);
  private final Date future = new Date(System.currentTimeMillis() + 60_000);

  @Test
  public void shouldAcceptValidToken() {
    FtgoPrincipal principal = verifier.verify(TestJwtTokens.token(TEST_SECRET, FtgoRole.CONSUMER, 42L, future));
    assertEquals(FtgoRole.CONSUMER, principal.getRole());
    assertEquals(42L, principal.getId());
    assertEquals("consumer-42", principal.getName());
  }

  @Test
  public void shouldRejectTokenSignedWithOtherSecret() {
    assertRejected(TestJwtTokens.token(OTHER_SECRET, FtgoRole.CONSUMER, 42L, future));
  }

  @Test
  public void shouldRejectExpiredToken() {
    assertRejected(TestJwtTokens.token(TEST_SECRET, FtgoRole.CONSUMER, 42L, new Date(System.currentTimeMillis() - 60_000)));
  }

  @Test
  public void shouldRejectTokenWithoutExpiration() {
    assertRejected(TestJwtTokens.token(TEST_SECRET, FtgoRole.CONSUMER, 42L, null));
  }

  @Test
  public void shouldRejectUnsignedToken() {
    String unsigned = Jwts.builder().setSubject("consumer-42")
            .claim(JwtTokenVerifier.ROLE_CLAIM, "CONSUMER").claim("consumerId", 42L)
            .setExpiration(future).compact();
    assertRejected(unsigned);
  }

  @Test
  public void shouldRejectTokenWithUnknownRole() {
    String token = Jwts.builder().setSubject("x").claim(JwtTokenVerifier.ROLE_CLAIM, "ADMIN").claim("consumerId", 42L)
            .setExpiration(future)
            .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256,
                    new javax.crypto.spec.SecretKeySpec(TEST_SECRET.getBytes(), "HmacSHA256")).compact();
    assertRejected(token);
  }

  @Test
  public void shouldRejectTokenMissingIdClaim() {
    String token = Jwts.builder().setSubject("x").claim(JwtTokenVerifier.ROLE_CLAIM, "CONSUMER")
            .setExpiration(future)
            .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256,
                    new javax.crypto.spec.SecretKeySpec(TEST_SECRET.getBytes(), "HmacSHA256")).compact();
    assertRejected(token);
  }

  @Test
  public void shouldRejectGarbage() {
    assertRejected("not-a-jwt");
  }

  @Test(expected = IllegalStateException.class)
  public void shouldRejectShortSecret() {
    new JwtTokenVerifier("short");
  }

  private void assertRejected(String token) {
    try {
      verifier.verify(token);
      fail("expected BadCredentialsException");
    } catch (BadCredentialsException expected) {
    }
  }
}
