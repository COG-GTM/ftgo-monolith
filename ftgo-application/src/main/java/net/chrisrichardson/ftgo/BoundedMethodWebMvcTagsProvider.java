package net.chrisrichardson.ftgo;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTags;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Tags provider for http.server.requests metrics that only emits well-known HTTP
 * methods as the "method" tag, so arbitrary request methods cannot create an
 * unbounded number of meters.
 */
public class BoundedMethodWebMvcTagsProvider implements WebMvcTagsProvider {

  static final Tag METHOD_UNKNOWN = Tag.of("method", "UNKNOWN");

  @Override
  public Iterable<Tag> getTags(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Throwable exception) {
    return Tags.of(method(request), WebMvcTags.uri(request, response),
            WebMvcTags.exception(exception), WebMvcTags.status(response));
  }

  @Override
  public Iterable<Tag> getLongRequestTags(HttpServletRequest request, Object handler) {
    return Tags.of(method(request), WebMvcTags.uri(request, null));
  }

  static Tag method(HttpServletRequest request) {
    if (request != null) {
      HttpMethod httpMethod = HttpMethod.resolve(request.getMethod());
      if (httpMethod != null) {
        return Tag.of("method", httpMethod.name());
      }
    }
    return METHOD_UNKNOWN;
  }
}
