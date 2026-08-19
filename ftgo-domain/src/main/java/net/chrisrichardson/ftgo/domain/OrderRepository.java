package net.chrisrichardson.ftgo.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends CrudRepository<Order, Long> {
  List<Order> findAllByConsumerId(Long consumerId);

  Optional<Order> findByIdAndConsumerId(Long id, Long consumerId);
}
