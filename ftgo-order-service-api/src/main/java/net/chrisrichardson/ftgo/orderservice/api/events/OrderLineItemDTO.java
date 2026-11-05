package net.chrisrichardson.ftgo.orderservice.api.events;

public record OrderLineItemDTO(int quantity, String menuItemId, String name) {
}
