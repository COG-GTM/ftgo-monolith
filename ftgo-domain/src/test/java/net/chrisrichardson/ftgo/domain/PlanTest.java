package net.chrisrichardson.ftgo.domain;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class PlanTest {

  @Test
  public void newPlanHasNoActions() {
    Plan plan = new Plan();

    assertTrue(plan.getActions().isEmpty());
    assertTrue(plan.actionsForDelivery(order(1L)).isEmpty());
  }

  @Test
  public void addsAndFiltersActionsForOneDelivery() {
    Plan plan = new Plan();
    Order requested = order(1L);
    Action pickup = Action.makePickup(requested);
    Action dropoff = Action.makeDropoff(requested, null);
    plan.add(pickup);
    plan.add(Action.makePickup(order(2L)));
    plan.add(dropoff);

    assertEquals(3, plan.getActions().size());
    assertEquals(2, plan.actionsForDelivery(order(1L)).size());
    assertSame(pickup, plan.actionsForDelivery(order(1L)).get(0));
    assertSame(dropoff, plan.actionsForDelivery(order(1L)).get(1));
  }

  @Test
  public void removeDeliveryKeepsOtherActionsAndPlanCanStillBeAddedTo() {
    Plan plan = new Plan();
    Order removed = order(1L);
    Order retained = order(2L);
    plan.add(Action.makePickup(removed));
    plan.add(Action.makeDropoff(removed, null));
    Action retainedAction = Action.makePickup(retained);
    plan.add(retainedAction);

    plan.removeDelivery(order(1L));

    assertEquals(Collections.singletonList(retainedAction), plan.getActions());
    Action addedAfterRemoval = Action.makePickup(order(3L));
    plan.add(addedAfterRemoval);
    assertEquals(2, plan.getActions().size());
    assertSame(addedAfterRemoval, plan.getActions().get(1));
  }

  private static Order order(Long id) {
    Order order = new Order(1L, null, Collections.emptyList());
    order.setId(id);
    return order;
  }
}
