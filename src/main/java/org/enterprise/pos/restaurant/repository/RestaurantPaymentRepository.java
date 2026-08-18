package org.enterprise.pos.restaurant.repository;

import org.enterprise.pos.restaurant.entity.RestaurantPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantPaymentRepository extends JpaRepository<RestaurantPayment, Long> {
}
