package net.chrisrichardson.ftgo.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

public class DistanceOptimizedCourierAssignmentStrategy implements CourierAssignmentStrategy {

  private static final Logger logger = LoggerFactory.getLogger(DistanceOptimizedCourierAssignmentStrategy.class);

  private static final double EARTH_RADIUS_KM = 6371.0;
  private static final double DISTANCE_WEIGHT = 0.6;
  private static final double LOAD_WEIGHT = 0.4;
  private static final int MAX_ACTIVE_DELIVERIES = 5;

  @Override
  public Courier assignCourier(List<Courier> availableCouriers, Order order) {
    if (availableCouriers.isEmpty()) {
      throw new NoCourierAvailableException();
    }

    // In the decomposed architecture, Order no longer holds a Restaurant reference.
    // Fall back to load-balanced assignment since restaurant location is not available
    // on the Order entity. Distance-based optimization can be added to CourierService
    // which has access to restaurant data via the RestaurantServiceClient.
    logger.info("Using load-balanced courier assignment (restaurant location not on Order entity)");
    return assignByLoadBalance(availableCouriers);
  }

  private Courier assignByLoadBalance(List<Courier> couriers) {
    return couriers.stream()
            .min(Comparator.comparingInt(Courier::getActiveDeliveryCount))
            .orElseThrow(NoCourierAvailableException::new);
  }

  public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_KM * c;
  }

  public static double estimateDeliveryMinutes(double distanceKm) {
    double averageSpeedKmPerMin = 0.5;
    double baseMinutes = 5.0;
    return baseMinutes + (distanceKm / averageSpeedKmPerMin);
  }
}
