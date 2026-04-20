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

    Double restaurantLat = null;
    Double restaurantLng = null;

    final Double targetLat = restaurantLat;
    final Double targetLng = restaurantLng;

    if (targetLat == null || targetLng == null) {
      logger.info("Restaurant has no location data, using load-balanced assignment");
      return assignByLoadBalance(availableCouriers);
    }

    Courier best = null;
    double bestScore = Double.MAX_VALUE;

    for (Courier courier : availableCouriers) {
      if (courier.getActiveDeliveryCount() >= MAX_ACTIVE_DELIVERIES) {
        continue;
      }

      double score;
      if (courier.hasLocation()) {
        double distance = haversineDistance(
                courier.getCurrentLatitude(), courier.getCurrentLongitude(),
                targetLat, targetLng);
        double normalizedDistance = Math.min(distance / 20.0, 1.0);
        double normalizedLoad = (double) courier.getActiveDeliveryCount() / MAX_ACTIVE_DELIVERIES;
        score = (DISTANCE_WEIGHT * normalizedDistance) + (LOAD_WEIGHT * normalizedLoad);
      } else {
        double normalizedLoad = (double) courier.getActiveDeliveryCount() / MAX_ACTIVE_DELIVERIES;
        score = 0.5 + (LOAD_WEIGHT * normalizedLoad);
      }

      if (score < bestScore) {
        bestScore = score;
        best = courier;
      }
    }

    if (best == null) {
      logger.warn("All couriers at max capacity, falling back to least-loaded");
      return assignByLoadBalance(availableCouriers);
    }

    logger.info("Assigned courier {} with score {}", best.getId(), bestScore);
    return best;
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
