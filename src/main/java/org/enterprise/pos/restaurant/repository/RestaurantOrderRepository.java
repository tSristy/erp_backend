package org.enterprise.pos.restaurant.repository;

import org.enterprise.pos.restaurant.entity.RestaurantOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, Long> {
}
