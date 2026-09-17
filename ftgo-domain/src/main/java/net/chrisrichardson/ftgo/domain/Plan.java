package net.chrisrichardson.ftgo.domain;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

// Value object embedded in Courier holding the courier's pickup/dropoff actions.
@Embeddable
@Access(AccessType.FIELD)
public class Plan {

  // Persisted in the courier_actions table (Flyway V1); name it explicitly rather than relying on the
  // implicit <entity>_<attribute> naming so the mapping does not depend on the naming strategy.
  @ElementCollection
  @CollectionTable(name = "courier_actions")
  private List<Action> actions = new LinkedList<>();

  public void add(Action action) {
    actions.add(action);
  }

  public void removeDelivery(Order order) {
    actions = actions.stream().filter(action -> !action.actionFor(order)).collect(Collectors.toList());
  }

  public List<Action> getActions() {
    return actions;
  }

  public List<Action> actionsForDelivery(Order order) {
    return actions.stream().filter(action -> action.actionFor(order)).collect(Collectors.toList());
  }
}
