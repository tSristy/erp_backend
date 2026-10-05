package org.enterprise.organization.service;

import org.enterprise.inventory.service.BaseService;
import org.enterprise.organization.entity.Territory;
import org.enterprise.organization.repository.TerritoryRepository;
import org.enterprise.organization.specification.TerritorySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TerritoryService extends BaseService<Territory, Long> {

    private final TerritoryRepository repository;

    private final org.enterprise.hr.repository.EmployeeRepository employeeRepository;
    
    public TerritoryService(TerritoryRepository repository, org.enterprise.hr.repository.EmployeeRepository employeeRepository) {
        super(repository);
        this.repository = repository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<Territory> search(String query, Pageable pageable) {
        return repository.findAll(TerritorySpecification.searchByQuery(query), pageable);
    }

    @Override
    @Transactional
    public Territory save(Territory entity) {
        if (entity.getCode() == null || entity.getCode().trim().isEmpty()) {
            repository.findTopByCodeStartingWithOrderByIdDesc("TERR-")
                .ifPresentOrElse(
                    lastTerritory -> {
                        String lastCode = lastTerritory.getCode();
                        try {
                            int seq = Integer.parseInt(lastCode.replace("TERR-", ""));
                            entity.setCode(String.format("TERR-%03d", seq + 1));
                        } catch (Exception e) {
                            entity.setCode("TERR-001");
                        }
                    },
                    () -> entity.setCode("TERR-001")
                );
        }
        return super.save(entity);
    }
}
