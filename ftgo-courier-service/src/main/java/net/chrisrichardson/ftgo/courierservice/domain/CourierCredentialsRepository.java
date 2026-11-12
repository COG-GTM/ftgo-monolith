package net.chrisrichardson.ftgo.courierservice.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CourierCredentialsRepository extends CrudRepository<CourierCredentials, Long> {

  Optional<CourierCredentials> findByUsername(String username);
}
