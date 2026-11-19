package net.chrisrichardson.ftgo.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Long> {
  List<Order> findAllByConsumerId(Long consumerId);

  @Query("SELECT a FROM Courier c JOIN c.plan.actions a WHERE c = :courier AND a.order = :order")
  List<Action> findCourierActionsForDelivery(@Param("courier") Courier courier, @Param("order") Order order);
}
