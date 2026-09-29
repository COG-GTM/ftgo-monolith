package net.chrisrichardson.ftgo.consumerservice.web;

final class ConsumerApiExamples {

  static final String CREATE_CONSUMER_REQUEST = "{\n"
          + "  \"name\": { \"firstName\": \"John\", \"lastName\": \"Doe\" }\n"
          + "}";

  static final String CREATE_CONSUMER_RESPONSE = "{ \"consumerId\": 1 }";

  static final String GET_CONSUMER_RESPONSE = "{\n"
          + "  \"consumerId\": 0,\n"
          + "  \"name\": { \"firstName\": \"John\", \"lastName\": \"Doe\" }\n"
          + "}";

  private ConsumerApiExamples() {
  }
}
