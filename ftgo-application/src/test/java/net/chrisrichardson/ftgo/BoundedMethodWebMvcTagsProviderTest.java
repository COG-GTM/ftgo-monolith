package net.chrisrichardson.ftgo;

import io.micrometer.core.instrument.Tag;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BoundedMethodWebMvcTagsProviderTest {

  private final BoundedMethodWebMvcTagsProvider provider = new BoundedMethodWebMvcTagsProvider();

  @Test
  public void shouldKeepStandardMethods() {
    assertEquals(Tag.of("method", "POST"), methodTag("POST"));
    assertEquals(Tag.of("method", "GET"), methodTag("GET"));
    assertEquals(Tag.of("method", "DELETE"), methodTag("DELETE"));
  }

  @Test
  public void shouldCollapseUnknownMethodsToSingleTagValue() {
    Set<Tag> tags = new HashSet<>();
    for (int i = 0; i < 1000; i++) {
      tags.add(methodTag("BOGUS" + i));
    }
    assertEquals(1, tags.size());
    assertTrue(tags.contains(BoundedMethodWebMvcTagsProvider.METHOD_UNKNOWN));
  }

  private Tag methodTag(String method) {
    MockHttpServletRequest request = new MockHttpServletRequest(method, "/orders");
    for (Tag tag : provider.getTags(request, new MockHttpServletResponse(), null, null)) {
      if (tag.getKey().equals("method")) {
        return tag;
      }
    }
    throw new AssertionError("no method tag");
  }
}
