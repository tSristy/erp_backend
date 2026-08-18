package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {
    List<UnitOfMeasure> findByCompanyId(Long companyId);
}
