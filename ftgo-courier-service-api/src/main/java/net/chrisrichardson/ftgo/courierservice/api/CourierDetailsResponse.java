package net.chrisrichardson.ftgo.courierservice.api;

import net.chrisrichardson.ftgo.common.PersonName;

import java.time.LocalDateTime;
import java.util.List;

public class CourierDetailsResponse {

  private long id;
  private PersonName name;
  private boolean available;
  private CourierPlan plan;

  public CourierDetailsResponse() {
  }

  public CourierDetailsResponse(long id, PersonName name, boolean available, CourierPlan plan) {
    this.id = id;
    this.name = name;
    this.available = available;
    this.plan = plan;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public PersonName getName() {
    return name;
  }

  public void setName(PersonName name) {
    this.name = name;
  }

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }

  public CourierPlan getPlan() {
    return plan;
  }

  public void setPlan(CourierPlan plan) {
    this.plan = plan;
  }

  public static class CourierPlan {

    private List<CourierAction> actions;

    public CourierPlan() {
    }

    public CourierPlan(List<CourierAction> actions) {
      this.actions = actions;
    }

    public List<CourierAction> getActions() {
      return actions;
    }

    public void setActions(List<CourierAction> actions) {
      this.actions = actions;
    }
  }

  public static class CourierAction {

    private String type;
    private LocalDateTime time;

    public CourierAction() {
    }

    public CourierAction(String type, LocalDateTime time) {
      this.type = type;
      this.time = time;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public LocalDateTime getTime() {
      return time;
    }

    public void setTime(LocalDateTime time) {
      this.time = time;
    }
  }
}
