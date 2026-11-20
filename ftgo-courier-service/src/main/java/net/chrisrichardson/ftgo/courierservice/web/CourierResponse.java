package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.domain.ActionType;
import net.chrisrichardson.ftgo.domain.Courier;
import java.time.LocalDateTime;
import java.util.List;
import io.swagger.annotations.ApiModel;

@ApiModel("Courier")
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

  public CourierResponse(Courier courier, List<ActionResponse> actions) {
    id = courier.getId();
    name = courier.getName();
    address = courier.getAddress();
    plan = new PlanResponse(actions);
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

  @ApiModel("Plan")
  public static class PlanResponse {
    private final List<ActionResponse> actions;
    public PlanResponse(List<ActionResponse> actions) { this.actions = actions; }
    public List<ActionResponse> getActions() { return actions; }
  }

  @ApiModel("Action")
  public static class ActionResponse {
    private final ActionType type;
    private final LocalDateTime time;
    private final Long orderId;
    public ActionResponse(ActionType type, LocalDateTime time, Long orderId) {
      this.type = type;
      this.time = time;
      this.orderId = orderId;
    }
    public ActionType getType() { return type; }
    public LocalDateTime getTime() { return time; }
    public Long getOrderId() { return orderId; }
  }
}
