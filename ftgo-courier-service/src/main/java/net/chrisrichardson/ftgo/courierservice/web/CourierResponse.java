package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.Plan;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CourierResponse {
  private final Long id;
  private final PersonName name;
  private final Address address;
  private final PlanResponse plan;
  private final boolean available;
  private final Double currentLatitude;
  private final Double currentLongitude;
  private final LocalDateTime lastLocationUpdate;
  private final int activeDeliveryCount;

  public CourierResponse(Courier courier) {
    id = courier.getId();
    name = courier.getName();
    address = courier.getAddress();
    plan = courier.getPlan() == null ? null : new PlanResponse(courier.getPlan());
    available = courier.isAvailable();
    currentLatitude = courier.getCurrentLatitude();
    currentLongitude = courier.getCurrentLongitude();
    lastLocationUpdate = courier.getLastLocationUpdate();
    activeDeliveryCount = courier.getActiveDeliveryCount();
  }

  public Long getId() { return id; }
  public PersonName getName() { return name; }
  public Address getAddress() { return address; }
  public PlanResponse getPlan() { return plan; }
  public boolean isAvailable() { return available; }
  public Double getCurrentLatitude() { return currentLatitude; }
  public Double getCurrentLongitude() { return currentLongitude; }
  public LocalDateTime getLastLocationUpdate() { return lastLocationUpdate; }
  public int getActiveDeliveryCount() { return activeDeliveryCount; }

  public static class PlanResponse {
    private final List<ActionResponse> actions;

    public PlanResponse(Plan plan) {
      actions = plan.getActions() == null ? null : plan.getActions().stream()
              .map(action -> new ActionResponse(action.getType().name(), action.getTime()))
              .collect(Collectors.toList());
    }

    public List<ActionResponse> getActions() { return actions; }
  }

  public static class ActionResponse {
    private final String type;
    private final LocalDateTime time;

    public ActionResponse(String type, LocalDateTime time) {
      this.type = type;
      this.time = time;
    }

    public String getType() { return type; }
    public LocalDateTime getTime() { return time; }
  }
}
