package org.enterprise.production.repository;

import org.enterprise.production.entity.BomItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BomItemRepository extends JpaRepository<BomItem, Long> {
    List<BomItem> findByBomId(Long bomId);
}
