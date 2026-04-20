package net.chrisrichardson.ftgo.courierservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourierRepository extends JpaRepository<Courier, Long> {

    List<Courier> findByAvailableTrue();
}
