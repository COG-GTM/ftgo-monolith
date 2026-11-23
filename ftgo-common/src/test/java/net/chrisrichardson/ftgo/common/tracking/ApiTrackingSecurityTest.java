package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ApiTrackingSecurityTest {

  private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

  private String correlationIdFor(String header) {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
    if (header != null) {
      request.addHeader(ApiTrackingInterceptor.CORRELATION_ID_HEADER, header);
    }
    MockHttpServletResponse response = new MockHttpServletResponse();
    new ApiTrackingInterceptor(null).preHandle(request, response, null);
    return response.getHeader(ApiTrackingInterceptor.CORRELATION_ID_HEADER);
  }

  @Test
  public void keepsWellFormedCorrelationId() {
    assertEquals("abc-123_x.y", correlationIdFor("abc-123_x.y"));
  }

  @Test
  public void replacesCorrelationIdContainingLogInjection() {
    String id = correlationIdFor("abc\r\nFAKE LOG ENTRY");
    assertNotEquals("abc\r\nFAKE LOG ENTRY", id);
    assertTrue(id.matches(UUID_REGEX));
  }

  @Test
  public void replacesOverlongCorrelationId() {
    StringBuilder longId = new StringBuilder();
    for (int i = 0; i < 65; i++) longId.append('a');
    assertTrue(correlationIdFor(longId.toString()).matches(UUID_REGEX));
  }

  @Test
  public void generatesCorrelationIdWhenMissing() {
    assertTrue(correlationIdFor(null).matches(UUID_REGEX));
  }

  @Test
  public void acceptsMinutesBackWithinBounds() {
    assertEquals(1, ApiTrackingController.boundedMinutesBack(1));
    assertEquals(ApiTrackingController.MAX_MINUTES_BACK,
            ApiTrackingController.boundedMinutesBack(ApiTrackingController.MAX_MINUTES_BACK));
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsNegativeMinutesBack() {
    ApiTrackingController.boundedMinutesBack(-5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsUnboundedMinutesBack() {
    ApiTrackingController.boundedMinutesBack(Integer.MAX_VALUE);
  }
}
