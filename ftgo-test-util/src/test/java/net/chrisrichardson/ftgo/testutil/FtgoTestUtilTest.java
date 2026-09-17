package net.chrisrichardson.ftgo.testutil;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies the JUnit 5 based helpers in {@link FtgoTestUtil}.
 */
public class FtgoTestUtilTest {

  // A present Optional must pass silently.
  @Test
  public void assertPresentPassesForPresentOptional() {
    assertDoesNotThrow(() -> FtgoTestUtil.assertPresent(Optional.of("value")));
  }

  // An empty Optional must fail with the Jupiter/opentest4j assertion error.
  @Test
  public void assertPresentFailsForEmptyOptional() {
    assertThrows(AssertionFailedError.class, () -> FtgoTestUtil.assertPresent(Optional.empty()));
  }
}
