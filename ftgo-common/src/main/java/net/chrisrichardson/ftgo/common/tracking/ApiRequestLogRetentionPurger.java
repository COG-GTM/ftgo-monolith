package net.chrisrichardson.ftgo.common.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Deletes API request log records older than the configured retention period
 * ({@code ftgo.tracking.retention.days}, default 30 days).
 */
public class ApiRequestLogRetentionPurger {

  private static final Logger logger = LoggerFactory.getLogger(ApiRequestLogRetentionPurger.class);

  private final ApiRequestLogRepository apiRequestLogRepository;
  private final int retentionDays;

  public ApiRequestLogRetentionPurger(ApiRequestLogRepository apiRequestLogRepository, int retentionDays) {
    this.apiRequestLogRepository = apiRequestLogRepository;
    this.retentionDays = retentionDays;
  }

  @Scheduled(fixedDelayString = "${ftgo.tracking.retention.purge-interval-ms:3600000}")
  @Transactional
  public void purgeExpiredLogs() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
    int deleted = apiRequestLogRepository.deleteLogsOlderThan(cutoff);
    if (deleted > 0) {
      logger.info("Purged {} API request log entries older than {}", deleted, cutoff);
    }
  }
}
