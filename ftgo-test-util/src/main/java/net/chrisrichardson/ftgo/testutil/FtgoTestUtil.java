package net.chrisrichardson.ftgo.testutil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Small assertion helpers shared by the service test suites.
 * Built on JUnit 5 (Jupiter) assertions.
 */
public class FtgoTestUtil {

  /**
   * Asserts that the given {@link Optional} holds a value.
   * Fails with an {@link org.opentest4j.AssertionFailedError} when it is empty.
   */
  public static <T> void assertPresent(Optional<T> value) {
    assertTrue(value.isPresent(), "Expected Optional to be present but it was empty");
  }
}
