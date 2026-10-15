package org.enterprise.pos.restaurant.repository;

import org.enterprise.pos.restaurant.entity.KitchenOrderTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KitchenOrderTicketRepository extends JpaRepository<KitchenOrderTicket, Long> {

    @Query("SELECT DISTINCT k FROM KitchenOrderTicket k " +
           "LEFT JOIN FETCH k.order o " +
           "LEFT JOIN FETCH k.details d " +
           "LEFT JOIN FETCH d.product p " +
           "WHERE k.status IN :statuses " +
           "ORDER BY k.id ASC")
    List<KitchenOrderTicket> findByStatusInWithDetails(@Param("statuses") List<KitchenOrderTicket.KotStatus> statuses);
}
