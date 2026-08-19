package net.chrisrichardson.ftgo.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderRepository extends PagingAndSortingRepository<Order, Long> {
  Page<Order> findAllByConsumerId(Long consumerId, Pageable pageable);
}
