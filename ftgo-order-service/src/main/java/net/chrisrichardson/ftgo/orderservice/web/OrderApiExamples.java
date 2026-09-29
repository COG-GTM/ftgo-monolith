package net.chrisrichardson.ftgo.orderservice.web;

final class OrderApiExamples {

  static final String CREATE_ORDER_REQUEST = "{\n"
          + "  \"consumerId\": 1,\n"
          + "  \"restaurantId\": 1,\n"
          + "  \"lineItems\": [\n"
          + "    { \"menuItemId\": \"1\", \"quantity\": 5 },\n"
          + "    { \"menuItemId\": \"2\", \"quantity\": 1 }\n"
          + "  ]\n"
          + "}";

  static final String CREATE_ORDER_RESPONSE = "{ \"orderId\": 1 }";

  static final String GET_ORDER_RESPONSE = "{\n"
          + "  \"orderId\": 1,\n"
          + "  \"state\": \"ACCEPTED\",\n"
          + "  \"orderTotal\": \"65.20\",\n"
          + "  \"restaurantName\": \"Ajanta\",\n"
          + "  \"assignedCourier\": 1,\n"
          + "  \"courierActions\": [\n"
          + "    {\n"
          + "      \"type\": \"PICKUP\",\n"
          + "      \"time\": null\n"
          + "    },\n"
          + "    {\n"
          + "      \"type\": \"DROPOFF\",\n"
          + "      \"time\": \"2026-09-29T19:15:00\"\n"
          + "    }\n"
          + "  ],\n"
          + "  \"estimatedDeliveryTime\": \"2026-09-29T19:15:00\"\n"
          + "}";

  static final String GET_APPROVED_ORDER_RESPONSE = "{\n"
          + "  \"orderId\": 1,\n"
          + "  \"state\": \"APPROVED\",\n"
          + "  \"orderTotal\": \"65.20\",\n"
          + "  \"restaurantName\": \"Ajanta\",\n"
          + "  \"assignedCourier\": null,\n"
          + "  \"courierActions\": null,\n"
          + "  \"estimatedDeliveryTime\": null\n"
          + "}";

  static final String GET_CANCELLED_ORDER_RESPONSE = "{\n"
          + "  \"orderId\": 2,\n"
          + "  \"state\": \"CANCELLED\",\n"
          + "  \"orderTotal\": \"7.00\",\n"
          + "  \"restaurantName\": \"Ajanta\",\n"
          + "  \"assignedCourier\": null,\n"
          + "  \"courierActions\": null,\n"
          + "  \"estimatedDeliveryTime\": null\n"
          + "}";

  static final String GET_REVISED_ORDER_RESPONSE = "{\n"
          + "  \"orderId\": 3,\n"
          + "  \"state\": \"APPROVED\",\n"
          + "  \"orderTotal\": \"28.18\",\n"
          + "  \"restaurantName\": \"Ajanta\",\n"
          + "  \"assignedCourier\": null,\n"
          + "  \"courierActions\": null,\n"
          + "  \"estimatedDeliveryTime\": null\n"
          + "}";

  static final String GET_ORDERS_RESPONSE = "[\n"
          + "  {\n"
          + "    \"orderId\": 1,\n"
          + "    \"state\": \"ACCEPTED\",\n"
          + "    \"orderTotal\": \"65.20\",\n"
          + "    \"restaurantName\": \"Ajanta\",\n"
          + "    \"assignedCourier\": 1,\n"
          + "    \"courierActions\": [\n"
          + "      {\n"
          + "        \"type\": \"PICKUP\",\n"
          + "        \"time\": null\n"
          + "      },\n"
          + "      {\n"
          + "        \"type\": \"DROPOFF\",\n"
          + "        \"time\": \"2026-09-29T19:15:00\"\n"
          + "      }\n"
          + "    ],\n"
          + "    \"estimatedDeliveryTime\": \"2026-09-29T19:15:00\"\n"
          + "  },\n"
          + "  {\n"
          + "    \"orderId\": 2,\n"
          + "    \"state\": \"CANCELLED\",\n"
          + "    \"orderTotal\": \"7.00\",\n"
          + "    \"restaurantName\": \"Ajanta\",\n"
          + "    \"assignedCourier\": null,\n"
          + "    \"courierActions\": null,\n"
          + "    \"estimatedDeliveryTime\": null\n"
          + "  }\n"
          + "]";

  static final String REVISE_ORDER_REQUEST = "{\n"
          + "  \"revisedLineItemQuantities\": {\n"
          + "    \"1\": 2,\n"
          + "    \"2\": 1\n"
          + "  }\n"
          + "}";

  static final String ORDER_ACCEPTANCE_REQUEST = "{ \"readyBy\": \"2026-09-29T19:00:00\" }";

  private OrderApiExamples() {
  }
}
