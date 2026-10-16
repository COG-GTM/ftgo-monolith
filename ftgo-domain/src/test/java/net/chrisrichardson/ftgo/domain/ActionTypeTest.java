package net.chrisrichardson.ftgo.domain;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ActionTypeTest {

  @Test
  public void valuesAndValueOfExposePickupThenDropoff() {
    assertArrayEquals(new ActionType[]{ActionType.PICKUP, ActionType.DROPOFF}, ActionType.values());
    assertEquals(ActionType.PICKUP, ActionType.valueOf("PICKUP"));
    assertEquals(ActionType.DROPOFF, ActionType.valueOf("DROPOFF"));
  }
}
