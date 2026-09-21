package net.chrisrichardson.ftgo.common.tracking;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApiTrackingControllerTest {

  @Test
  public void shouldLeaveOrdinaryUriUnchanged() {
    assertEquals("/orders/1", ApiTrackingController.escapeLikePattern("/orders/1"));
  }

  @Test
  public void shouldEscapePercentWildcard() {
    assertEquals("!%", ApiTrackingController.escapeLikePattern("%"));
  }

  @Test
  public void shouldEscapeUnderscoreWildcard() {
    assertEquals("/api!_tracking", ApiTrackingController.escapeLikePattern("/api_tracking"));
  }

  @Test
  public void shouldEscapeEscapeCharacterItself() {
    assertEquals("a!!b", ApiTrackingController.escapeLikePattern("a!b"));
  }

  @Test
  public void shouldEscapeMixedInput() {
    assertEquals("!!!%!_", ApiTrackingController.escapeLikePattern("!%_"));
  }
}
