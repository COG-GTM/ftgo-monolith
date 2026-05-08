package net.chrisrichardson.ftgo.courierservice.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CourierRepository extends CrudRepository<Courier, Long> {

  List<Courier> findAllByAvailable(boolean available);
}
