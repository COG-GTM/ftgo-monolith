package net.chrisrichardson.ftgo.courierservice.api;

import java.time.LocalDateTime;

public record CourierWorkloadResponse(long courierId, int activeDeliveries, boolean available, Double currentLatitude, Double currentLongitude, LocalDateTime lastLocationUpdate) {
}
