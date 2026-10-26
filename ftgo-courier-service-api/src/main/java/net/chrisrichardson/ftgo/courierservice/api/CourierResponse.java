package net.chrisrichardson.ftgo.courierservice.api;

import net.chrisrichardson.ftgo.common.PersonName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CourierResponse {

  private Long id;
  private PersonName name;
  private boolean available;
  private int activeDeliveries;
  private PlanResponse plan = new PlanResponse();
  private Double currentLatitude;
  private Double currentLongitude;
  private LocalDateTime lastLocationUpdate;

  public CourierResponse() {
  }

  public CourierResponse(Long id, PersonName name, boolean available, int activeDeliveries,
                         PlanResponse plan) {
    this.id = id;
    this.name = name;
    this.available = available;
    this.activeDeliveries = activeDeliveries;
    this.plan = plan == null ? new PlanResponse() : plan;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
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

  public int getActiveDeliveries() {
    return activeDeliveries;
  }

  public void setActiveDeliveries(int activeDeliveries) {
    this.activeDeliveries = activeDeliveries;
  }

  public PlanResponse getPlan() {
    return plan;
  }

  public void setPlan(PlanResponse plan) {
    this.plan = plan;
  }

  public Double getCurrentLatitude() {
    return currentLatitude;
  }

  public void setCurrentLatitude(Double currentLatitude) {
    this.currentLatitude = currentLatitude;
  }

  public Double getCurrentLongitude() {
    return currentLongitude;
  }

  public void setCurrentLongitude(Double currentLongitude) {
    this.currentLongitude = currentLongitude;
  }

  public LocalDateTime getLastLocationUpdate() {
    return lastLocationUpdate;
  }

  public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) {
    this.lastLocationUpdate = lastLocationUpdate;
  }

  public static class PlanResponse {

    private List<ActionResponse> actions = new ArrayList<>();

    public PlanResponse() {
    }

    public PlanResponse(List<ActionResponse> actions) {
      this.actions = actions == null ? new ArrayList<>() : actions;
    }

    public List<ActionResponse> getActions() {
      return actions;
    }

    public void setActions(List<ActionResponse> actions) {
      this.actions = actions;
    }
  }

  public static class ActionResponse {

    private String type;
    private LocalDateTime time;

    public ActionResponse() {
    }

    public ActionResponse(String type, LocalDateTime time) {
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
