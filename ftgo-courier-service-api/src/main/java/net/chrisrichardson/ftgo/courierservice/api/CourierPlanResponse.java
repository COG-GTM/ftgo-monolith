package net.chrisrichardson.ftgo.courierservice.api;

import java.util.List;

public class CourierPlanResponse {

  private List<CourierActionResponse> actions;

  public CourierPlanResponse() {
  }

  public CourierPlanResponse(List<CourierActionResponse> actions) {
    this.actions = actions;
  }

  public List<CourierActionResponse> getActions() {
    return actions;
  }

  public void setActions(List<CourierActionResponse> actions) {
    this.actions = actions;
  }
}
