package net.chrisrichardson.ftgo.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Long> {
  List<Order> findAllByConsumerId(Long consumerId);

  @Query("select o from Order o left join fetch o.restaurant where o.orderState in :orderStates")
  List<Order> findAllByOrderStateIn(@Param("orderStates") Collection<OrderState> orderStates);
}
