package org.enterprise.pos.restaurant.repository;

import org.enterprise.pos.restaurant.entity.RestaurantOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, Long> {
    
    List<RestaurantOrder> findByStatus(RestaurantOrder.RestaurantOrderStatus status);
    
    List<RestaurantOrder> findByStatusIn(List<RestaurantOrder.RestaurantOrderStatus> statuses);

    List<RestaurantOrder> findByType(RestaurantOrder.TransactionType type);

    Optional<RestaurantOrder> findByTableNumberAndStatusIn(String tableNumber, List<RestaurantOrder.RestaurantOrderStatus> statuses);
}
