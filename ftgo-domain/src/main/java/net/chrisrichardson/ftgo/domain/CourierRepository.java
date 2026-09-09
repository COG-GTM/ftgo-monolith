package net.chrisrichardson.ftgo.domain;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CourierRepository extends CrudRepository<Courier, Long> {

  @Query("SELECT c FROM Courier c WHERE c.available = true")
  List<Courier> findAllAvailable();

  @Query("SELECT c FROM Courier c WHERE c.available = true AND c.currentLatitude IS NOT NULL AND c.currentLongitude IS NOT NULL")
  List<Courier> findAllAvailableWithLocation();

  @Modifying(clearAutomatically = true)
  @Query("UPDATE Courier c SET c.currentLatitude = NULL, c.currentLongitude = NULL, c.lastLocationUpdate = NULL " +
          "WHERE (c.currentLatitude IS NOT NULL OR c.currentLongitude IS NOT NULL OR c.lastLocationUpdate IS NOT NULL) " +
          "AND (c.lastLocationUpdate IS NULL OR c.lastLocationUpdate < :cutoff)")
  int clearLocationsUpdatedBefore(@Param("cutoff") LocalDateTime cutoff);

}
