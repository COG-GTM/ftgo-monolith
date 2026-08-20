package net.chrisrichardson.ftgo.common.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

public class ApiRequestLogRetentionJob {

  private static final Logger logger = LoggerFactory.getLogger(ApiRequestLogRetentionJob.class);

  private final ApiRequestLogRepository apiRequestLogRepository;
  private final int retentionDays;

  public ApiRequestLogRetentionJob(ApiRequestLogRepository apiRequestLogRepository, int retentionDays) {
    this.apiRequestLogRepository = apiRequestLogRepository;
    this.retentionDays = retentionDays;
  }

  @Scheduled(fixedDelayString = "${ftgo.api-tracking.purge-interval-ms:3600000}")
  public void purgeExpiredLogs() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
    try {
      int deleted = apiRequestLogRepository.deleteByRequestTimestampBefore(cutoff);
      logger.info("Purged {} API request log rows older than {}", deleted, cutoff);
    } catch (Exception e) {
      logger.warn("Failed to purge API request logs older than {}", cutoff, e);
    }
  }
}
