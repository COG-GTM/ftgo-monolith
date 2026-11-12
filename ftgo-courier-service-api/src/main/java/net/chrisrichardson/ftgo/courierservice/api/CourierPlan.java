package net.chrisrichardson.ftgo.courierservice.api;

import java.util.List;

public class CourierPlan {

  private List<CourierAction> actions;

  public CourierPlan() {
  }

  public CourierPlan(List<CourierAction> actions) {
    this.actions = actions;
  }

  public List<CourierAction> getActions() {
    return actions;
  }
}
