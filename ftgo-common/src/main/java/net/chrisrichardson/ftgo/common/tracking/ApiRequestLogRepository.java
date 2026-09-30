package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ApiRequestLogRepository extends CrudRepository<ApiRequestLog, Long> {

  @Query("SELECT a FROM ApiRequestLog a WHERE a.requestTimestamp >= :since ORDER BY a.requestTimestamp DESC")
  List<ApiRequestLog> findRecentLogs(@Param("since") LocalDateTime since);

  @Query("SELECT a FROM ApiRequestLog a WHERE a.requestTimestamp >= :since ORDER BY a.requestTimestamp DESC, a.id DESC")
  List<ApiRequestLog> findRecentLogs(@Param("since") LocalDateTime since, Pageable pageable);

  @Query("SELECT a FROM ApiRequestLog a WHERE a.requestUri LIKE :pattern ESCAPE '!' AND a.requestTimestamp >= :since ORDER BY a.requestTimestamp DESC, a.id DESC")
  List<ApiRequestLog> findByRequestUriContaining(@Param("pattern") String pattern, @Param("since") LocalDateTime since, Pageable pageable);

  @Query("SELECT a FROM ApiRequestLog a WHERE a.correlationId = :correlationId")
  ApiRequestLog findByCorrelationId(@Param("correlationId") String correlationId);

  @Query("SELECT a FROM ApiRequestLog a WHERE a.responseStatus >= 400 AND a.requestTimestamp >= :since ORDER BY a.requestTimestamp DESC, a.id DESC")
  List<ApiRequestLog> findErrorsSince(@Param("since") LocalDateTime since, Pageable pageable);

}
