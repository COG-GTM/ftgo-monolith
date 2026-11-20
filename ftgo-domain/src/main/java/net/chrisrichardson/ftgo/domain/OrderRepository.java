package net.chrisrichardson.ftgo.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Long> {
  List<Order> findAllByConsumerId(Long consumerId);
  List<Order> findAllByConsumerIdOrderByIdAsc(Long consumerId, Pageable pageable);
  List<Order> findAllByConsumerIdAndOrderStateOrderByIdAsc(Long consumerId, OrderState orderState, Pageable pageable);
}
