package net.chrisrichardson.ftgo.courierservice.web;

final class CourierApiExamples {

  static final String CREATE_COURIER_REQUEST = "{\n"
          + "  \"name\": { \"firstName\": \"Jane\", \"lastName\": \"Smith\" },\n"
          + "  \"address\": {\n"
          + "    \"street1\": \"1 Scenic Drive\",\n"
          + "    \"street2\": null,\n"
          + "    \"city\": \"Oakland\",\n"
          + "    \"state\": \"CA\",\n"
          + "    \"zip\": \"94555\",\n"
          + "    \"latitude\": 37.8044,\n"
          + "    \"longitude\": -122.2712\n"
          + "  }\n"
          + "}";

  static final String CREATE_COURIER_RESPONSE = "{ \"id\": 1 }";

  static final String AVAILABLE_REQUEST = "{ \"available\": true }";

  static final String UNAVAILABLE_REQUEST = "{ \"available\": false }";

  static final String LOCATION_UPDATE_REQUEST = "{ \"latitude\": 37.8101, \"longitude\": -122.2650 }";

  static final String GET_COURIER_RESPONSE = "{\n"
          + "  \"id\": 1,\n"
          + "  \"name\": {\n"
          + "    \"firstName\": \"Jane\",\n"
          + "    \"lastName\": \"Smith\"\n"
          + "  },\n"
          + "  \"address\": {\n"
          + "    \"street1\": \"1 Scenic Drive\",\n"
          + "    \"street2\": null,\n"
          + "    \"city\": \"Oakland\",\n"
          + "    \"state\": \"CA\",\n"
          + "    \"zip\": \"94555\",\n"
          + "    \"latitude\": 37.8044,\n"
          + "    \"longitude\": -122.2712\n"
          + "  },\n"
          + "  \"plan\": {\n"
          + "    \"actions\": [\n"
          + "      {\n"
          + "        \"type\": \"PICKUP\",\n"
          + "        \"time\": null\n"
          + "      },\n"
          + "      {\n"
          + "        \"type\": \"DROPOFF\",\n"
          + "        \"time\": \"2026-09-29T19:15:00\"\n"
          + "      }\n"
          + "    ]\n"
          + "  },\n"
          + "  \"available\": true,\n"
          + "  \"currentLatitude\": 37.8101,\n"
          + "  \"currentLongitude\": -122.265,\n"
          + "  \"lastLocationUpdate\": \"2026-09-29T18:40:12\",\n"
          + "  \"activeDeliveryCount\": 1\n"
          + "}";

  static final String WORKLOAD_RESPONSE = "{\n"
          + "  \"courierId\": 1,\n"
          + "  \"activeDeliveries\": 1,\n"
          + "  \"available\": true,\n"
          + "  \"currentLatitude\": 37.8101,\n"
          + "  \"currentLongitude\": -122.265,\n"
          + "  \"lastLocationUpdate\": \"2026-09-29T18:40:12\"\n"
          + "}";

  static final String COURIER_NOT_FOUND = "{\n"
          + "  \"timestamp\": \"2026-09-29T18:00:00\",\n"
          + "  \"status\": 404,\n"
          + "  \"error\": \"Not Found\",\n"
          + "  \"message\": \"Courier not found: 99\",\n"
          + "  \"path\": \"/couriers/99/location\",\n"
          + "  \"correlationId\": \"0d7202cc-7e7a-409d-8c9a-7b565ac90256\"\n"
          + "}";

  private CourierApiExamples() {
  }
}
