package net.chrisrichardson.ftgo.common.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class ApiRequestLogRetentionService {

  private static final Logger logger = LoggerFactory.getLogger(ApiRequestLogRetentionService.class);

  private final ApiRequestLogRepository apiRequestLogRepository;
  private final int retentionDays;

  public ApiRequestLogRetentionService(ApiRequestLogRepository apiRequestLogRepository,
                                       @Value("${ftgo.api-tracking.retention-days:7}") int retentionDays) {
    this.apiRequestLogRepository = apiRequestLogRepository;
    this.retentionDays = retentionDays;
  }

  @Scheduled(fixedDelayString = "${ftgo.api-tracking.purge-interval-ms:3600000}")
  @Transactional
  public void purgeExpiredLogs() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
    int deleted = apiRequestLogRepository.deleteOlderThan(cutoff);
    if (deleted > 0) {
      logger.info("Purged {} api_request_log rows older than {}", deleted, cutoff);
    }
  }
}
