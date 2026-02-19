package net.chrisrichardson.ftgo.orderservice.api.web;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(description = "Order details response")
public class GetOrderResponse {
  @ApiModelProperty(value = "Order ID")
  private long orderId;
  @ApiModelProperty(value = "Current order state", example = "APPROVED")
  private String state;
  @ApiModelProperty(value = "Total order amount")
  private String orderTotal;
  @ApiModelProperty(value = "Name of the restaurant")
  private String restaurantName;
  @ApiModelProperty(value = "Assigned courier ID, null if not yet assigned")
  private Long assignedCourier;
  @ApiModelProperty(value = "Courier delivery actions")
  private List<CourierActionDTO> courierActions;

  private GetOrderResponse() {
  }

  public GetOrderResponse(long orderId, String state, String orderTotal, String restaurantName, Long assignedCourier, List<CourierActionDTO> courierActions) {
    this.orderId = orderId;
    this.state = state;
    this.orderTotal = orderTotal;
    this.restaurantName = restaurantName;
    this.assignedCourier = assignedCourier;
    this.courierActions = courierActions;
  }

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(String orderTotal) {
    this.orderTotal = orderTotal;
  }

  public String getRestaurantName() {
    return restaurantName;
  }

  public void setRestaurantName(String restaurantName) {
    this.restaurantName = restaurantName;
  }

  public Long getAssignedCourier() {
    return assignedCourier;
  }

  public void setAssignedCourier(Long assignedCourier) {
    this.assignedCourier = assignedCourier;
  }

  public List<CourierActionDTO> getCourierActions() {
    return courierActions;
  }

  public void setCourierActions(List<CourierActionDTO> courierActions) {
    this.courierActions = courierActions;
  }

  @ApiModel(description = "Courier delivery action")
  public static class CourierActionDTO {
    @ApiModelProperty(value = "Action type (PICKUP or DROPOFF)")
    private String type;
    @ApiModelProperty(value = "Scheduled time for action")
    private String time;

    private CourierActionDTO() {
    }

    public CourierActionDTO(String type, String time) {
      this.type = type;
      this.time = time;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getTime() {
      return time;
    }

    public void setTime(String time) {
      this.time = time;
    }
  }
}
