package net.chrisrichardson.ftgo.courierservice.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;

public class CourierLocationRetentionJob {

  private static final Logger logger = LoggerFactory.getLogger(CourierLocationRetentionJob.class);

  private final CourierService courierService;
  private final Duration retention;

  public CourierLocationRetentionJob(CourierService courierService, Duration retention) {
    this.courierService = courierService;
    this.retention = retention;
  }

  @Scheduled(fixedDelayString = "${ftgo.courier.location.purge-interval-ms:60000}")
  public void purgeStaleLocations() {
    int purged = courierService.purgeLocationsOlderThan(retention);
    if (purged > 0) {
      logger.info("Purged live location from {} couriers not updated within {}", purged, retention);
    }
  }
}
