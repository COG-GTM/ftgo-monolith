package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.ActionType;
import net.chrisrichardson.ftgo.domain.Courier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class GetCourierResponse {

  private Long id;
  private PersonName name;
  private Address address;
  private PlanResponse plan;
  private boolean available;
  private Double currentLatitude;
  private Double currentLongitude;
  private LocalDateTime lastLocationUpdate;
  private int activeDeliveryCount;

  public static GetCourierResponse from(Courier courier) {
    GetCourierResponse response = new GetCourierResponse();
    response.id = courier.getId();
    response.name = courier.getName();
    response.address = courier.getAddress();
    if (courier.getPlan() != null) {
      response.plan = new PlanResponse();
      if (courier.getPlan().getActions() != null) {
        response.plan.actions = courier.getPlan().getActions().stream()
                .map(action -> new ActionResponse(action.getType(), action.getTime()))
                .collect(Collectors.toList());
      }
    }
    response.available = courier.isAvailable();
    response.currentLatitude = courier.getCurrentLatitude();
    response.currentLongitude = courier.getCurrentLongitude();
    response.lastLocationUpdate = courier.getLastLocationUpdate();
    response.activeDeliveryCount = courier.getActiveDeliveryCount();
    return response;
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
    private List<ActionResponse> actions;

    public List<ActionResponse> getActions() { return actions; }
  }

  public static class ActionResponse {
    private final ActionType type;
    private final LocalDateTime time;

    public ActionResponse(ActionType type, LocalDateTime time) {
      this.type = type;
      this.time = time;
    }

    public ActionType getType() { return type; }
    public LocalDateTime getTime() { return time; }
  }
}
