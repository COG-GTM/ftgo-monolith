package net.chrisrichardson.ftgo.courierservice.domain;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.util.LinkedList;
import java.util.List;

@Embeddable
public class Plan {

  @ElementCollection
  private List<Action> actions = new LinkedList<>();

  public void add(Action action) {
    actions.add(action);
  }

  public List<Action> getActions() {
    return actions;
  }
}
