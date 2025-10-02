# FTGO Restaurant Microservice

This is the standalone Restaurant Service extracted from the FTGO monolith as part of Phase 1 of the microservices migration.

## Overview

The Restaurant Service is responsible for managing restaurant information, including:
- Restaurant registration and details
- Menu management
- Menu item information and pricing

This service has been designed to be the first extracted microservice due to its minimal external dependencies and well-defined service boundary.

## Architecture

### Database
The Restaurant Service uses its own dedicated database schema (`ftgo_restaurant`) that is separate from the main monolith database. This ensures proper data isolation and independence.

**Tables:**
- `restaurants` - Stores restaurant details (name, address)
- `restaurant_menu_items` - Stores menu items for each restaurant (name, price)

### API Endpoints

#### Create Restaurant
- **POST** `/restaurants`
- Creates a new restaurant with menu items
- **Request Body:**
```json
{
  "name": "Restaurant Name",
  "address": {
    "street1": "123 Main St",
    "street2": "Suite 100",
    "city": "San Francisco",
    "state": "CA",
    "zip": "94105"
  },
  "menu": {
    "menuItemDTOs": [
      {
        "id": "item-1",
        "name": "Burger",
        "price": {
          "amount": 12.99
        }
      }
    ]
  }
}
```
- **Response:**
```json
{
  "id": 1
}
```

#### Get Restaurant
- **GET** `/restaurants/{restaurantId}`
- Retrieves restaurant details including menu items
- **Response:**
```json
{
  "id": 1,
  "name": "Restaurant Name",
  "menuItems": [
    {
      "id": "item-1",
      "name": "Burger",
      "price": {
        "amount": 12.99
      }
    }
  ]
}
```

## Running the Service

### Prerequisites
- Java 8
- MySQL 8.0+
- Gradle

### Database Setup
1. Create the restaurant database:
```sql
CREATE DATABASE ftgo_restaurant;
```

2. Update database credentials in `src/main/resources/application.properties` if needed:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ftgo_restaurant
spring.datasource.username=root
spring.datasource.password=rootpassword
```

### Starting the Service

Build and run the service:
```bash
./gradlew :ftgo-restaurant-microservice:bootRun
```

The service will start on **port 8082** (configurable in `application.properties`).

### Testing the Service

Create a restaurant:
```bash
curl -X POST http://localhost:8082/restaurants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Restaurant",
    "address": {
      "street1": "123 Main St",
      "city": "San Francisco",
      "state": "CA",
      "zip": "94105"
    },
    "menu": {
      "menuItemDTOs": [
        {
          "id": "burger-1",
          "name": "Classic Burger",
          "price": {
            "amount": 12.99
          }
        }
      ]
    }
  }'
```

Get restaurant details:
```bash
curl http://localhost:8082/restaurants/1
```

## Integration with Monolith

The monolith has been updated to communicate with this service via HTTP instead of direct database access. The `OrderService` now uses `RestaurantServiceProxy` to fetch restaurant information when creating orders.

### Configuration
The monolith's `application.properties` includes:
```properties
restaurant.service.url=http://${RESTAURANT_SERVICE_HOST:localhost}:8082
```

Set the `RESTAURANT_SERVICE_HOST` environment variable when deploying to point to the actual restaurant service location.

## API Documentation

When the service is running, Swagger UI is available at:
```
http://localhost:8082/swagger-ui.html
```

## Future Enhancements

Potential improvements for future phases:
1. Add menu update/revision functionality
2. Implement event-driven communication for restaurant changes
3. Add circuit breaker pattern for resilience
4. Implement caching for frequently accessed restaurant data
5. Add authentication and authorization
6. Implement rate limiting
7. Add comprehensive monitoring and metrics

## Migration Notes

### Changes to Monolith
The following changes were made to the monolith to support this extraction:

1. **OrderService** - Updated to use `RestaurantServiceProxy` instead of direct `RestaurantRepository` access
2. **OrderConfiguration** - Updated to inject `RestaurantServiceProxy` instead of `RestaurantRepository`
3. **FtgoApplicationMain** - Removed `RestaurantServiceConfiguration` import
4. **RestaurantServiceProxy** - New HTTP client for communicating with Restaurant Service

### Data Migration
When deploying, you'll need to:
1. Set up the new `ftgo_restaurant` database
2. Run the Flyway migration scripts to create tables
3. Copy existing restaurant data from the monolith database to the new database:
```sql
INSERT INTO ftgo_restaurant.restaurants 
SELECT * FROM ftgo.restaurants;

INSERT INTO ftgo_restaurant.restaurant_menu_items 
SELECT * FROM ftgo.restaurant_menu_items;
```

### Deployment Considerations
- The Restaurant Service should be deployed and accessible before updating the monolith
- Set appropriate `RESTAURANT_SERVICE_HOST` environment variable in the monolith
- Consider implementing health checks to verify service connectivity
- Monitor HTTP communication for performance and reliability

## Dependencies

Key dependencies:
- Spring Boot 2.0.x
- Spring Data JPA
- MySQL Connector
- Flyway (for database migrations)
- Swagger (for API documentation)

## Contact

For questions or issues related to the Restaurant Service migration, please contact the architecture team.
