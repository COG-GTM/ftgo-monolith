package net.chrisrichardson.ftgo.courierservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourierCredentialsRepository extends JpaRepository<CourierCredentials, Long> {

  Optional<CourierCredentials> findByUsername(String username);
}
