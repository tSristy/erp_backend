package org.enterprise.crm.service.repository;

import org.enterprise.crm.service.entity.TechnicianStockMovement;
import org.enterprise.inventory.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianStockMovementRepository extends BaseRepository<TechnicianStockMovement, Long> {
    List<TechnicianStockMovement> findByCompanyIdOrderByIdDesc(Long companyId);
    List<TechnicianStockMovement> findByTechnicianIdOrderByIdDesc(Long technicianId);
}
