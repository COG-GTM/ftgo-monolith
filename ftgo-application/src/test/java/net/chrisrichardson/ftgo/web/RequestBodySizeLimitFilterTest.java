package net.chrisrichardson.ftgo.web;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RequestBodySizeLimitFilterTest {

  private static final long LIMIT = 10;

  @Test
  public void rejectsContentLengthOverLimitWithoutCallingChain() throws Exception {
    RequestBodySizeLimitFilter filter = makeFilter();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContent(new byte[11]);
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, (servletRequest, servletResponse) -> fail("Filter chain must not run"));

    assertEquals(413, response.getStatus());
  }

  @Test
  public void letsChainReadCompleteBodyWithinLimit() throws Exception {
    RequestBodySizeLimitFilter filter = makeFilter();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContent("under".getBytes("UTF-8"));
    MockHttpServletResponse response = new MockHttpServletResponse();
    ByteArrayOutputStream body = new ByteArrayOutputStream();

    filter.doFilter(request, response, (servletRequest, servletResponse) -> {
      byte[] buffer = new byte[4];
      int count;
      while ((count = servletRequest.getInputStream().read(buffer)) != -1) {
        body.write(buffer, 0, count);
      }
    });

    assertEquals("under", new String(body.toByteArray(), "UTF-8"));
  }

  @Test
  public void throwsWhenBodyWithoutContentLengthExceedsLimit() throws Exception {
    RequestBodySizeLimitFilter filter = makeFilter();
    MockHttpServletRequest request = new MockHttpServletRequest() {
      @Override
      public long getContentLengthLong() {
        return -1;
      }
    };
    request.setContent(new byte[11]);
    MockHttpServletResponse response = new MockHttpServletResponse();

    try {
      filter.doFilter(request, response, (servletRequest, servletResponse) -> {
        byte[] buffer = new byte[16];
        while (servletRequest.getInputStream().read(buffer) != -1) {
        }
      });
      fail("Expected RequestBodyTooLargeException");
    } catch (IOException ex) {
      if (!(ex instanceof RequestBodyTooLargeException)) {
        throw ex;
      }
    }
  }

  private RequestBodySizeLimitFilter makeFilter() {
    RequestBodySizeLimitFilter filter = new RequestBodySizeLimitFilter();
    ReflectionTestUtils.setField(filter, "maxRequestBodyBytes", LIMIT);
    return filter;
  }
}
