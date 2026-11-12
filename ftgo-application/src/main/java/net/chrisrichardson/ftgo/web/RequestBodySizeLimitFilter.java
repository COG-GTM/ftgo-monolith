package net.chrisrichardson.ftgo.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestBodySizeLimitFilter extends OncePerRequestFilter {

  @Value("${ftgo.web.max-request-body-bytes:65536}")
  private long maxRequestBodyBytes;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    if (request.getContentLengthLong() > maxRequestBodyBytes) {
      response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
              "Request body exceeds " + maxRequestBodyBytes + " bytes");
      return;
    }

    filterChain.doFilter(new SizeLimitedHttpServletRequest(request, maxRequestBodyBytes), response);
  }

  private static class SizeLimitedHttpServletRequest extends HttpServletRequestWrapper {

    private final long limit;
    private ServletInputStream inputStream;

    private SizeLimitedHttpServletRequest(HttpServletRequest request, long limit) {
      super(request);
      this.limit = limit;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
      if (inputStream == null) {
        inputStream = new SizeLimitedServletInputStream(super.getInputStream(), limit);
      }
      return inputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
      String encoding = getCharacterEncoding();
      Charset charset = encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
      return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }
  }

  private static class SizeLimitedServletInputStream extends ServletInputStream {

    private final ServletInputStream delegate;
    private final long limit;
    private long bytesRead;

    private SizeLimitedServletInputStream(ServletInputStream delegate, long limit) {
      this.delegate = delegate;
      this.limit = limit;
    }

    @Override
    public int read() throws IOException {
      int value = delegate.read();
      recordBytesRead(value == -1 ? 0 : 1);
      return value;
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
      int count = delegate.read(bytes, offset, length);
      recordBytesRead(count);
      return count;
    }

    private void recordBytesRead(int count) throws RequestBodyTooLargeException {
      if (count > 0) {
        bytesRead += count;
        if (bytesRead > limit) {
          throw new RequestBodyTooLargeException(limit);
        }
      }
    }

    @Override
    public boolean isFinished() {
      return delegate.isFinished();
    }

    @Override
    public boolean isReady() {
      return delegate.isReady();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
      delegate.setReadListener(readListener);
    }
  }
}
