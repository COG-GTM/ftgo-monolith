package net.chrisrichardson.ftgo.restaurantservice.web;

final class RestaurantApiExamples {

  static final String CREATE_RESTAURANT_REQUEST = "{\n"
          + "  \"name\": \"Ajanta\",\n"
          + "  \"address\": {\n"
          + "    \"street1\": \"1 Main Street\",\n"
          + "    \"street2\": \"Unit 99\",\n"
          + "    \"city\": \"Oakland\",\n"
          + "    \"state\": \"CA\",\n"
          + "    \"zip\": \"94611\",\n"
          + "    \"latitude\": 37.8272,\n"
          + "    \"longitude\": -122.2566\n"
          + "  },\n"
          + "  \"menu\": {\n"
          + "    \"menuItemDTOs\": [\n"
          + "      { \"id\": \"1\", \"name\": \"Chicken Vindaloo\", \"price\": \"12.34\" },\n"
          + "      { \"id\": \"2\", \"name\": \"Garlic Naan\", \"price\": \"3.50\" }\n"
          + "    ]\n"
          + "  }\n"
          + "}";

  static final String CREATE_RESTAURANT_RESPONSE = "{ \"id\": 1 }";

  static final String GET_RESTAURANT_RESPONSE = "{ \"id\": 1, \"name\": \"Ajanta\" }";

  private RestaurantApiExamples() {
  }
}
