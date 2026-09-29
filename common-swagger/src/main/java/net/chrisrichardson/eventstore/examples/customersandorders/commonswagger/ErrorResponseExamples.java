package net.chrisrichardson.eventstore.examples.customersandorders.commonswagger;

public final class ErrorResponseExamples {

  public static final String ORDER_NOT_FOUND = "{\n"
          + "  \"timestamp\": \"2026-09-29T18:00:00\",\n"
          + "  \"status\": 404,\n"
          + "  \"error\": \"Not Found\",\n"
          + "  \"message\": \"Order not found99\",\n"
          + "  \"path\": \"/orders/99/accept\",\n"
          + "  \"correlationId\": \"3f0c2a8e-6b1d-4d2e-9a57-1c9e7b4f2d10\"\n"
          + "}";

  public static final String RESTAURANT_NOT_FOUND = "{\n"
          + "  \"timestamp\": \"2026-09-29T18:00:00\",\n"
          + "  \"status\": 404,\n"
          + "  \"error\": \"Not Found\",\n"
          + "  \"message\": \"Restaurant not found with id 99\",\n"
          + "  \"path\": \"/orders\",\n"
          + "  \"correlationId\": \"3f0c2a8e-6b1d-4d2e-9a57-1c9e7b4f2d10\"\n"
          + "}";

  public static final String UNSUPPORTED_STATE_TRANSITION = "{\n"
          + "  \"timestamp\": \"2026-09-29T18:00:00\",\n"
          + "  \"status\": 409,\n"
          + "  \"error\": \"State Transition Error\",\n"
          + "  \"message\": \"current state: DELIVERED\",\n"
          + "  \"path\": \"/orders/1/cancel\",\n"
          + "  \"correlationId\": \"3f0c2a8e-6b1d-4d2e-9a57-1c9e7b4f2d10\"\n"
          + "}";

  public static final String NO_COURIER_AVAILABLE = "{\n"
          + "  \"timestamp\": \"2026-09-29T18:00:00\",\n"
          + "  \"status\": 503,\n"
          + "  \"error\": \"No Courier Available\",\n"
          + "  \"message\": \"No courier available for assignment\",\n"
          + "  \"path\": \"/orders/1/accept\",\n"
          + "  \"correlationId\": \"3f0c2a8e-6b1d-4d2e-9a57-1c9e7b4f2d10\"\n"
          + "}";

  public static final String BAD_REQUEST = "{\n"
          + "  \"timestamp\": \"2026-09-29T18:00:00\",\n"
          + "  \"status\": 400,\n"
          + "  \"error\": \"Bad Request\",\n"
          + "  \"message\": \"readyBy is not in the future\",\n"
          + "  \"path\": \"/orders/1/accept\",\n"
          + "  \"correlationId\": \"3f0c2a8e-6b1d-4d2e-9a57-1c9e7b4f2d10\"\n"
          + "}";

  public static final String INTERNAL_SERVER_ERROR = "{\n"
          + "  \"timestamp\": \"2026-09-29T18:00:00\",\n"
          + "  \"status\": 500,\n"
          + "  \"error\": \"Internal Server Error\",\n"
          + "  \"message\": \"An unexpected error occurred\",\n"
          + "  \"path\": \"/couriers/99\",\n"
          + "  \"correlationId\": \"3f0c2a8e-6b1d-4d2e-9a57-1c9e7b4f2d10\"\n"
          + "}";

  private ErrorResponseExamples() {
  }
}
