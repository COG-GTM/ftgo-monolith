package net.chrisrichardson.ftgo.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Long> {
  List<Order> findAllByConsumerId(Long consumerId);

  List<Order> findAllByConsumerIdAndOrderState(Long consumerId, OrderState orderState);

  Page<Order> findAllByConsumerId(Long consumerId, Pageable pageable);

  Page<Order> findAllByConsumerIdAndOrderState(Long consumerId, OrderState orderState, Pageable pageable);
}
