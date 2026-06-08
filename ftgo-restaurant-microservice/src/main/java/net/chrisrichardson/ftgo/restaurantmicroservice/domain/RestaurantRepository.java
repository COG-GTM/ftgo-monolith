package net.chrisrichardson.ftgo.restaurantmicroservice.domain;

import org.springframework.data.repository.CrudRepository;

public interface RestaurantRepository extends CrudRepository<Restaurant, Long> {
}
