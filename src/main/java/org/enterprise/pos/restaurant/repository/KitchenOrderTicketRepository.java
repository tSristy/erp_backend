package org.enterprise.pos.restaurant.repository;

import org.enterprise.pos.restaurant.entity.KitchenOrderTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KitchenOrderTicketRepository extends JpaRepository<KitchenOrderTicket, Long> {
}
