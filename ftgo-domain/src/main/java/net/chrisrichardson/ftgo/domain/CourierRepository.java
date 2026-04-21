package net.chrisrichardson.ftgo.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CourierRepository extends CrudRepository<Courier, Long> {

  @Query("SELECT c FROM Courier c WHERE c.available = true")
  List<Courier> findAllAvailable();

  @Query("SELECT c FROM Courier c WHERE c.available = true AND c.currentLatitude IS NOT NULL AND c.currentLongitude IS NOT NULL")
  List<Courier> findAllAvailableWithLocation();

}
