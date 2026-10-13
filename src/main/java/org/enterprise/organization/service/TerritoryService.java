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
    private final org.enterprise.organization.repository.AreaRepository areaRepository;
    
    public TerritoryService(TerritoryRepository repository,
                            org.enterprise.hr.repository.EmployeeRepository employeeRepository,
                            org.enterprise.organization.repository.AreaRepository areaRepository) {
        super(repository);
        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.areaRepository = areaRepository;
    }

    @Transactional(readOnly = true)
    public Page<Territory> search(String query, Pageable pageable) {
        return repository.findAll(TerritorySpecification.searchByQuery(query), pageable);
    }

    @Override
    @Transactional
    public Territory save(Territory entity) {
        if (entity.getId() != null && repository.existsById(entity.getId())) {
            return update(entity.getId(), entity);
        }
        return create(entity);
    }

    @Transactional
    public Territory create(Territory entity) {
        resolveRelations(entity);
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

    @Transactional
    public Territory update(Long id, Territory incoming) {
        Territory existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Territory not found with id: " + id));

        if (incoming.getCode() != null && !incoming.getCode().trim().isEmpty()) {
            existing.setCode(incoming.getCode().trim());
        }
        if (incoming.getName() != null) {
            existing.setName(incoming.getName().trim());
        }
        existing.setSalesType(incoming.getSalesType());
        if (incoming.getActive() != null) {
            existing.setActive(incoming.getActive());
        }

        if (incoming.getTerritoryManager() != null && incoming.getTerritoryManager().getId() != null) {
            existing.setTerritoryManager(employeeRepository.findById(incoming.getTerritoryManager().getId()).orElse(null));
        } else {
            existing.setTerritoryManager(null);
        }

        if (incoming.getArea() != null && incoming.getArea().getId() != null) {
            existing.setArea(areaRepository.findById(incoming.getArea().getId()).orElse(null));
        } else {
            existing.setArea(null);
        }

        return repository.save(existing);
    }

    private void resolveRelations(Territory entity) {
        if (entity.getTerritoryManager() != null && entity.getTerritoryManager().getId() != null) {
            entity.setTerritoryManager(employeeRepository.findById(entity.getTerritoryManager().getId()).orElse(null));
        } else {
            entity.setTerritoryManager(null);
        }

        if (entity.getArea() != null && entity.getArea().getId() != null) {
            entity.setArea(areaRepository.findById(entity.getArea().getId()).orElse(null));
        } else {
            entity.setArea(null);
        }
    }
}
