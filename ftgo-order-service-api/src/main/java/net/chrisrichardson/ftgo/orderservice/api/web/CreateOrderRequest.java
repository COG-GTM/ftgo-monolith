package net.chrisrichardson.ftgo.orderservice.api.web;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"restaurantId", "consumerId", "lineItems"})
public record CreateOrderRequest(long consumerId, long restaurantId, List<LineItem> lineItems) {
  public record LineItem(String menuItemId, int quantity) {
  }
}
