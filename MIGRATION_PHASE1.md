# FTGO Microservices Migration - Phase 1: Restaurant Service Extraction

## Executive Summary

Phase 1 of the FTGO monolith-to-microservices migration has successfully extracted the Restaurant Service as a standalone microservice. This document provides an overview of the changes, deployment instructions, and verification steps.

## Changes Overview

### New Components

1. **ftgo-restaurant-microservice** - A standalone Spring Boot application that manages all restaurant-related operations
   - Location: `/ftgo-restaurant-microservice`
   - Port: 8082 (configurable)
   - Database: `ftgo_restaurant` (separate schema)

### Modified Components

1. **ftgo-order-service**
   - Added `RestaurantServiceProxy` for HTTP-based communication with Restaurant Service
   - Updated `OrderService` to use proxy instead of direct repository access
   - Updated `OrderConfiguration` to inject `RestaurantServiceProxy`

2. **ftgo-application**
   - Removed `RestaurantServiceConfiguration` import from `FtgoApplicationMain`
   - Added `restaurant.service.url` configuration property

3. **Database Schema**
   - Created separate `ftgo_restaurant` database for restaurant data
   - Migration script: `ftgo-restaurant-microservice/src/main/resources/db/migration/V1__create_restaurant_schema.sql`

## Architecture Changes

### Before (Monolith)
```
┌─────────────────────────────────────────┐
│         FTGO Application                │
│                                         │
│  ┌──────────────┐   ┌───────────────┐  │
│  │ OrderService │──▶│ RestaurantRepo│  │
│  └──────────────┘   └───────────────┘  │
│         │                    │          │
│         └────────────────────┘          │
│              Database (ftgo)            │
└─────────────────────────────────────────┘
```

### After (Phase 1)
```
┌────────────────────────────┐         ┌──────────────────────────┐
│    FTGO Application        │         │  Restaurant Microservice │
│                            │         │                          │
│  ┌──────────────┐          │         │  ┌──────────────┐       │
│  │ OrderService │          │  HTTP   │  │ RestaurantSvc│       │
│  │      │       │          │ Request │  │      │       │       │
│  │      ▼       │          │         │  │      ▼       │       │
│  │RestaurantSvcProxy│─────────────────▶ │RestaurantRepo│       │
│  └──────────────┘          │         │  └──────────────┘       │
│         │                  │         │         │                │
│  Database (ftgo)           │         │  Database                │
│  (no restaurant tables)    │         │  (ftgo_restaurant)       │
└────────────────────────────┘         └──────────────────────────┘
```

## Deployment Guide

### Prerequisites
- MySQL 8.0+ server
- Java 8 JDK
- Gradle

### Step 1: Database Setup

Create the new restaurant database:
```sql
CREATE DATABASE ftgo_restaurant;
GRANT ALL PRIVILEGES ON ftgo_restaurant.* TO 'mysqluser'@'%';
FLUSH PRIVILEGES;
```

### Step 2: Data Migration

Copy existing restaurant data from the monolith database:
```sql
-- Copy restaurants
INSERT INTO ftgo_restaurant.restaurants 
SELECT * FROM ftgo.restaurants;

-- Copy menu items
INSERT INTO ftgo_restaurant.restaurant_menu_items 
SELECT * FROM ftgo.restaurant_menu_items;

-- Verify data
SELECT COUNT(*) FROM ftgo_restaurant.restaurants;
SELECT COUNT(*) FROM ftgo_restaurant.restaurant_menu_items;
```

### Step 3: Deploy Restaurant Service

Build the Restaurant Service:
```bash
cd /path/to/ftgo-monolith
./gradlew :ftgo-restaurant-microservice:build
```

Run the Restaurant Service:
```bash
./gradlew :ftgo-restaurant-microservice:bootRun
```

Or build a JAR and run it:
```bash
./gradlew :ftgo-restaurant-microservice:bootJar
java -jar ftgo-restaurant-microservice/build/libs/ftgo-restaurant-microservice.jar
```

The service will start on port 8082.

### Step 4: Configure and Deploy Monolith

Update the monolith configuration to point to the Restaurant Service:

For local development (default):
```properties
restaurant.service.url=http://localhost:8082
```

For production, set environment variable:
```bash
export RESTAURANT_SERVICE_HOST=restaurant-service.yourdomain.com
```

Build and run the updated monolith:
```bash
./gradlew :ftgo-application:build
./gradlew :ftgo-application:bootRun
```

### Step 5: Verification

1. **Verify Restaurant Service is running:**
```bash
curl http://localhost:8082/restaurants/1
```

2. **Test restaurant creation:**
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
          "price": {"amount": 12.99}
        }
      ]
    }
  }'
```

3. **Test order creation through monolith:**
```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{
    "consumerId": 1,
    "restaurantId": 1,
    "lineItems": [
      {"menuItemId": "burger-1", "quantity": 2}
    ]
  }'
```

4. **Check logs for HTTP communication:**
- Review monolith logs for successful HTTP requests to Restaurant Service
- Verify no direct database access to restaurant tables from OrderService

## Rollback Plan

If issues arise, you can rollback by:

1. **Re-enable direct database access:**
   - Restore the `RestaurantServiceConfiguration` import in `FtgoApplicationMain`
   - Revert changes to `OrderService` and `OrderConfiguration`
   - Redeploy the monolith

2. **Stop the Restaurant Service:**
```bash
# If running as a separate process
kill <pid>
```

3. **Keep restaurant data in original database:**
   - No need to restore data if it was copied (not moved)

## Testing Strategy

### Unit Tests
Run existing tests to verify no regressions:
```bash
./gradlew :ftgo-order-service:test
./gradlew :ftgo-restaurant-microservice:test
```

### Integration Tests
Run end-to-end tests:
```bash
./gradlew :ftgo-end-to-end-tests:test
```

### Manual Testing Checklist
- [ ] Create new restaurant via Restaurant Service
- [ ] Retrieve restaurant details via Restaurant Service
- [ ] Create order in monolith that references restaurant
- [ ] Verify order includes correct menu item details
- [ ] Test error handling when restaurant not found
- [ ] Test error handling when Restaurant Service is unavailable

## Performance Considerations

### Network Latency
- Direct repository access: ~1-5ms
- HTTP request to local service: ~10-50ms
- HTTP request to remote service: ~50-200ms (depending on network)

**Mitigation strategies:**
1. Deploy Restaurant Service in same data center as monolith
2. Implement caching for frequently accessed restaurant data
3. Use connection pooling in RestTemplate
4. Consider implementing circuit breaker pattern

### Database Performance
The separate database schema should have similar performance characteristics since the table structure remains the same.

## Monitoring and Observability

### Key Metrics to Monitor

1. **Restaurant Service**
   - Request rate
   - Response times (p50, p95, p99)
   - Error rate
   - Database connection pool usage

2. **Monolith Integration**
   - HTTP client success rate
   - HTTP client timeout rate
   - Fallback/error handling triggers

3. **Database**
   - Query performance on `ftgo_restaurant` database
   - Connection pool usage

### Recommended Tools
- Spring Boot Actuator endpoints for health checks
- Prometheus + Grafana for metrics
- ELK stack for log aggregation
- Zipkin/Jaeger for distributed tracing

## Known Limitations

1. **No caching** - Restaurant data is fetched on every order creation
2. **No circuit breaker** - Service failures will directly impact order creation
3. **Synchronous communication** - Could benefit from async/event-driven patterns
4. **No retry logic** - Failed HTTP requests are not automatically retried

These limitations are acceptable for Phase 1 and will be addressed in future phases.

## Future Phases

### Phase 2: Consumer Service Extraction
Extract the Consumer Service as the next microservice, following similar patterns.

### Phase 3: Courier Service Extraction
Extract the Courier Service after Consumer Service.

### Phase 4: Order Service Refinement
Refine the Order Service and implement event-driven communication patterns.

## Success Criteria

Phase 1 is considered successful when:
- ✅ Restaurant Service runs independently on port 8082
- ✅ Monolith can create orders using restaurant data via HTTP
- ✅ All existing functionality works as before
- ✅ Restaurant data is properly isolated in separate database
- ✅ No direct database access to restaurant tables from monolith
- ✅ API documentation is available and accurate

## Troubleshooting

### Issue: "Connection refused" errors in monolith logs

**Cause:** Restaurant Service is not running or not accessible

**Solution:**
1. Check if Restaurant Service is running: `curl http://localhost:8082/restaurants/1`
2. Verify `restaurant.service.url` configuration in monolith
3. Check firewall/network settings

### Issue: "Restaurant not found" errors

**Cause:** Data was not migrated or restaurant IDs don't match

**Solution:**
1. Verify data migration: `SELECT * FROM ftgo_restaurant.restaurants;`
2. Check restaurant ID being requested
3. Review migration SQL scripts

### Issue: Slow order creation

**Cause:** Network latency from HTTP calls

**Solution:**
1. Monitor HTTP request times
2. Consider implementing caching
3. Optimize database queries in Restaurant Service

## Support

For questions or issues:
1. Review this migration guide
2. Check the Restaurant Service README (`ftgo-restaurant-microservice/README.md`)
3. Review logs from both services
4. Contact the architecture team

## Conclusion

Phase 1 successfully extracts the Restaurant Service as an independent microservice, demonstrating the pattern for future service extractions. This provides a foundation for continued decomposition of the FTGO monolith into a microservices architecture.
