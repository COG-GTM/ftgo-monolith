package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;

import static net.chrisrichardson.ftgo.common.tracking.ApiTrackingInterceptor.anonymize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ApiTrackingInterceptorTest {

  @Test
  public void shouldMaskIpv4HostPart() {
    assertEquals("203.0.113.0", anonymize("203.0.113.42"));
    assertEquals("127.0.0.0", anonymize("127.0.0.1"));
  }

  @Test
  public void shouldMaskCompressedIpv6BelowThePrefix() {
    assertEquals("2001:db8:0:0:0:0:0:0", anonymize("2001:db8::1"));
    assertEquals("0:0:0:0:0:0:0:0", anonymize("::1"));
  }

  @Test
  public void shouldMaskExpandedIpv6BelowThePrefix() {
    assertEquals("2001:db8:1:0:0:0:0:0", anonymize("2001:db8:1:2:3:4:5:6"));
  }

  @Test
  public void shouldNotRetainNonLiteralAddresses() {
    assertEquals("unknown", anonymize("client.example.com"));
    assertEquals("unknown", anonymize("fe80::1%eth0"));
    assertEquals("unknown", anonymize(""));
  }

  @Test
  public void shouldPassThroughMissingAddress() {
    assertNull(anonymize(null));
  }
}
