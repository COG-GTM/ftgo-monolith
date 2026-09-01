package net.chrisrichardson.ftgo.common.tracking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ApiRequestLogRepository extends CrudRepository<ApiRequestLog, Long> {

  @Query("SELECT a FROM ApiRequestLog a WHERE a.requestTimestamp >= :since ORDER BY a.requestTimestamp DESC")
  List<ApiRequestLog> findRecentLogs(@Param("since") LocalDateTime since, Pageable pageable);

  @Query("SELECT a FROM ApiRequestLog a WHERE a.requestUri LIKE %:uri% ORDER BY a.requestTimestamp DESC")
  List<ApiRequestLog> findByRequestUri(@Param("uri") String uri, Pageable pageable);

  @Query("SELECT a FROM ApiRequestLog a WHERE a.correlationId = :correlationId")
  ApiRequestLog findByCorrelationId(@Param("correlationId") String correlationId);

  @Query("SELECT a FROM ApiRequestLog a WHERE a.responseStatus >= 400 AND a.requestTimestamp >= :since ORDER BY a.requestTimestamp DESC")
  List<ApiRequestLog> findErrorsSince(@Param("since") LocalDateTime since, Pageable pageable);

  @Query("SELECT count(a) FROM ApiRequestLog a WHERE a.requestTimestamp >= :since")
  long countSince(@Param("since") LocalDateTime since);

  @Query("SELECT count(a) FROM ApiRequestLog a WHERE a.responseStatus >= 400 AND a.requestTimestamp >= :since")
  long countErrorsSince(@Param("since") LocalDateTime since);

  @Query("SELECT avg(a.durationMs) FROM ApiRequestLog a WHERE a.requestTimestamp >= :since AND a.durationMs IS NOT NULL")
  Double averageDurationSince(@Param("since") LocalDateTime since);

  @Query("SELECT count(a) FROM ApiRequestLog a WHERE a.requestTimestamp >= :since AND a.durationMs IS NOT NULL")
  long countDurationsSince(@Param("since") LocalDateTime since);

  @Query("SELECT a.durationMs FROM ApiRequestLog a WHERE a.requestTimestamp >= :since AND a.durationMs IS NOT NULL ORDER BY a.durationMs ASC")
  List<Long> findDurationsSince(@Param("since") LocalDateTime since, Pageable pageable);

  @Query("SELECT a.responseStatus, count(a) FROM ApiRequestLog a WHERE a.requestTimestamp >= :since AND a.responseStatus IS NOT NULL GROUP BY a.responseStatus")
  List<Object[]> countByResponseStatusSince(@Param("since") LocalDateTime since);

  @Query("SELECT a.httpMethod, a.requestUri, count(a) FROM ApiRequestLog a WHERE a.requestTimestamp >= :since AND a.requestUri IS NOT NULL GROUP BY a.httpMethod, a.requestUri ORDER BY count(a) DESC")
  List<Object[]> countByEndpointSince(@Param("since") LocalDateTime since, Pageable pageable);

}
