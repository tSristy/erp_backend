package org.enterprise.production.repository;

import org.enterprise.production.entity.QualityInspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QualityInspectionRepository extends JpaRepository<QualityInspection, Long> {
    List<QualityInspection> findByManufacturingOrderIdOrderByIdDesc(Long manufacturingOrderId);
    Optional<QualityInspection> findTopByOrderByIdDesc();
    Page<QualityInspection> findByCompanyId(Long companyId, Pageable pageable);
}
