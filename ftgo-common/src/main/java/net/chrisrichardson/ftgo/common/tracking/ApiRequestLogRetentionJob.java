package net.chrisrichardson.ftgo.common.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public class ApiRequestLogRetentionJob {

  private static final Logger logger = LoggerFactory.getLogger(ApiRequestLogRetentionJob.class);

  private final ApiRequestLogRepository apiRequestLogRepository;

  @Value("${ftgo.api-tracking.retention-days:30}")
  private int retentionDays;

  public ApiRequestLogRetentionJob(ApiRequestLogRepository apiRequestLogRepository) {
    this.apiRequestLogRepository = apiRequestLogRepository;
  }

  @Transactional
  @Scheduled(fixedDelayString = "${ftgo.api-tracking.retention-check-interval-ms:3600000}")
  public void deleteExpiredLogs() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
    int deleted = apiRequestLogRepository.deleteLogsOlderThan(cutoff);
    if (deleted > 0) {
      logger.info("Deleted {} API request log entries older than {}", deleted, cutoff);
    }
  }
}
