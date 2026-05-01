package net.chrisrichardson.ftgo.domain;

import javax.persistence.ElementCollection;
import java.util.LinkedList;
import java.util.List;

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
