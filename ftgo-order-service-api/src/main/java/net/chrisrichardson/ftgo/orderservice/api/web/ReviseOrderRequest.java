package net.chrisrichardson.ftgo.orderservice.api.web;

import java.util.Map;

public record ReviseOrderRequest(Map<String, Integer> revisedLineItemQuantities) {
}
