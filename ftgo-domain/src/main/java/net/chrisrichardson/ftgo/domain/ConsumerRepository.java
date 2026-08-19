package net.chrisrichardson.ftgo.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ConsumerRepository extends CrudRepository<Consumer, Long> {
  Optional<Consumer> findByAccessToken(String accessToken);
}
