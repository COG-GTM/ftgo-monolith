package net.chrisrichardson.ftgo.consumerservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumerRepository extends JpaRepository<Consumer, Long> {
}
