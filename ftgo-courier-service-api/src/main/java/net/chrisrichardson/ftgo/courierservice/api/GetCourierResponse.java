package net.chrisrichardson.ftgo.courierservice.api;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;

import java.time.LocalDateTime;
import java.util.List;

public class GetCourierResponse {

  private Long id;
  private PersonName name;
  private Address address;
  private boolean available;
  private Double currentLatitude;
  private Double currentLongitude;
  private LocalDateTime lastLocationUpdate;
  private int activeDeliveryCount;
  private PlanResponse plan;

  public GetCourierResponse() {
  }

  public GetCourierResponse(Long id, PersonName name, Address address, boolean available,
                            Double currentLatitude, Double currentLongitude,
                            LocalDateTime lastLocationUpdate, int activeDeliveryCount, PlanResponse plan) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.available = available;
    this.currentLatitude = currentLatitude;
    this.currentLongitude = currentLongitude;
    this.lastLocationUpdate = lastLocationUpdate;
    this.activeDeliveryCount = activeDeliveryCount;
    this.plan = plan;
  }

  public Long getId() { return id; }
  public PersonName getName() { return name; }
  public Address getAddress() { return address; }
  public boolean isAvailable() { return available; }
  public Double getCurrentLatitude() { return currentLatitude; }
  public Double getCurrentLongitude() { return currentLongitude; }
  public LocalDateTime getLastLocationUpdate() { return lastLocationUpdate; }
  public int getActiveDeliveryCount() { return activeDeliveryCount; }
  public PlanResponse getPlan() { return plan; }

  public static class PlanResponse {
    private List<ActionResponse> actions;

    public PlanResponse() {
    }

    public PlanResponse(List<ActionResponse> actions) {
      this.actions = actions;
    }

    public List<ActionResponse> getActions() { return actions; }
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

    public String getType() { return type; }
    public LocalDateTime getTime() { return time; }
  }
}
