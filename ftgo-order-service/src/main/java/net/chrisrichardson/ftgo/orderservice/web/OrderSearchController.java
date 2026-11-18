package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.domain.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(path = "/orders/search")
public class OrderSearchController {

  private final EntityManager entityManager;

  public OrderSearchController(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Transactional(readOnly = true)
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<GetOrderResponse>> search(@RequestParam long consumerId,
                                                       @RequestParam(required = false) String restaurantName,
                                                       @RequestParam(defaultValue = "id") String sortBy) {
    StringBuilder jpql = new StringBuilder("select o from Order o where o.consumerId = " + consumerId);
    if (restaurantName != null && !restaurantName.isEmpty()) {
      jpql.append(" and o.restaurant.name like '%").append(restaurantName).append("%'");
    }
    jpql.append(" order by o.").append(sortBy);

    List<GetOrderResponse> orders = entityManager.createQuery(jpql.toString(), Order.class)
            .getResultList()
            .stream()
            .map(OrderController::makeGetOrderResponse)
            .collect(toList());

    return new ResponseEntity<>(orders, HttpStatus.OK);
  }
}
