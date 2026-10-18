package org.enterprise.crm.service.service;

import org.enterprise.common.util.TenantContext;
import org.enterprise.crm.service.entity.TechnicianStockMovement;
import org.enterprise.crm.service.entity.TechnicianStockMovementLine;
import org.enterprise.crm.service.repository.TechnicianStockMovementRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TechnicianStockMovementService extends BaseService<TechnicianStockMovement, Long> {

    private final TechnicianStockMovementRepository repository;

    public TechnicianStockMovementService(TechnicianStockMovementRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<TechnicianStockMovement> findAllByCurrentCompany() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId != null) {
            return repository.findByCompanyIdOrderByIdDesc(companyId);
        }
        return repository.findAll();
    }

    @Override
    @Transactional
    public TechnicianStockMovement save(TechnicianStockMovement entity) {
        Long companyId = TenantContext.getCompanyId();
        if (companyId != null && entity.getCompanyId() == null) {
            entity.setCompanyId(companyId);
        }
        if (entity.getLines() != null) {
            for (TechnicianStockMovementLine line : entity.getLines()) {
                line.setId(null);
                line.setMovement(entity);
                if (companyId != null && line.getCompanyId() == null) {
                    line.setCompanyId(companyId);
                }
            }
        }
        return super.save(entity);
    }
}
