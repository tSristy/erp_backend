package org.enterprise.pos.restaurant.repository;

import org.enterprise.pos.restaurant.entity.RestaurantOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, Long> {

    @Query("SELECT r FROM RestaurantOrder r " +
           "LEFT JOIN FETCH r.warehouse w " +
           "LEFT JOIN FETCH r.customer c " +
           "WHERE r.status = :status " +
           "ORDER BY r.id DESC")
    List<RestaurantOrder> findByStatus(@Param("status") RestaurantOrder.RestaurantOrderStatus status);

    @Query("SELECT r FROM RestaurantOrder r " +
           "LEFT JOIN FETCH r.warehouse w " +
           "LEFT JOIN FETCH r.customer c " +
           "WHERE r.status IN :statuses " +
           "ORDER BY r.id DESC")
    List<RestaurantOrder> findByStatusIn(@Param("statuses") List<RestaurantOrder.RestaurantOrderStatus> statuses);

    @Query("SELECT r FROM RestaurantOrder r " +
           "LEFT JOIN FETCH r.warehouse w " +
           "LEFT JOIN FETCH r.customer c " +
           "WHERE r.type = :type " +
           "ORDER BY r.id DESC")
    List<RestaurantOrder> findByType(@Param("type") RestaurantOrder.TransactionType type);

    @Query("SELECT r FROM RestaurantOrder r " +
           "LEFT JOIN FETCH r.warehouse w " +
           "LEFT JOIN FETCH r.customer c " +
           "WHERE w.id = :warehouseId " +
           "ORDER BY r.id DESC")
    List<RestaurantOrder> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("SELECT r FROM RestaurantOrder r " +
           "LEFT JOIN FETCH r.warehouse w " +
           "LEFT JOIN FETCH r.customer c " +
           "WHERE (:companyId IS NULL OR r.companyId = :companyId) " +
           "AND (:warehouseId IS NULL OR w.id = :warehouseId) " +
           "ORDER BY r.id DESC")
    List<RestaurantOrder> findActiveOrders(@Param("companyId") Long companyId,
                                          @Param("warehouseId") Long warehouseId);

    @Query("SELECT r FROM RestaurantOrder r " +
           "LEFT JOIN FETCH r.warehouse w " +
           "LEFT JOIN FETCH r.customer c " +
           "WHERE r.tableNumber = :tableNumber " +
           "AND r.status IN :statuses " +
           "ORDER BY r.id DESC")
    Optional<RestaurantOrder> findByTableNumberAndStatusIn(@Param("tableNumber") String tableNumber,
                                                           @Param("statuses") List<RestaurantOrder.RestaurantOrderStatus> statuses);

    @Query("SELECT DISTINCT r FROM RestaurantOrder r " +
           "LEFT JOIN FETCH r.warehouse w " +
           "LEFT JOIN FETCH r.customer c " +
           "LEFT JOIN FETCH r.details d " +
           "LEFT JOIN FETCH d.product p " +
           "WHERE r.id = :id")
    Optional<RestaurantOrder> findByIdWithDetails(@Param("id") Long id);
}
