package org.enterprise.pos.restaurant.repository;

import org.enterprise.pos.restaurant.entity.RestaurantTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    @Query("SELECT t FROM RestaurantTable t LEFT JOIN FETCH t.warehouse w " +
           "WHERE (:companyId IS NULL OR t.companyId = :companyId) " +
           "AND (:warehouseId IS NULL OR w.id = :warehouseId) " +
           "AND (t.active = true OR t.active IS NULL) " +
           "ORDER BY t.displayOrder ASC, t.tableNumber ASC")
    List<RestaurantTable> findActiveTables(@Param("companyId") Long companyId,
                                          @Param("warehouseId") Long warehouseId);

    @Query("SELECT t FROM RestaurantTable t LEFT JOIN FETCH t.warehouse w WHERE t.id = :id")
    Optional<RestaurantTable> findByIdWithWarehouse(@Param("id") Long id);

    @Query("SELECT t FROM RestaurantTable t LEFT JOIN FETCH t.warehouse w " +
           "WHERE (:companyId IS NULL OR t.companyId = :companyId) " +
           "AND w.id = :warehouseId AND t.tableNumber = :tableNumber")
    Optional<RestaurantTable> findByTableNumber(@Param("companyId") Long companyId,
                                               @Param("warehouseId") Long warehouseId,
                                               @Param("tableNumber") String tableNumber);

    @Query("SELECT t FROM RestaurantTable t LEFT JOIN t.warehouse w " +
           "WHERE (:companyId IS NULL OR t.companyId = :companyId) " +
           "AND (:warehouseId IS NULL OR w.id = :warehouseId) " +
           "AND (:query IS NULL OR :query = '' " +
           "     OR LOWER(t.tableNumber) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(t.section) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<RestaurantTable> searchTables(@Param("companyId") Long companyId,
                                       @Param("warehouseId") Long warehouseId,
                                       @Param("query") String query,
                                       Pageable pageable);
}
