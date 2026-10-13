package org.enterprise.organization.service;

import org.enterprise.inventory.service.BaseService;
import org.enterprise.organization.entity.Area;
import org.enterprise.organization.repository.AreaRepository;
import org.enterprise.organization.specification.AreaSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AreaService extends BaseService<Area, Long> {

    private final AreaRepository repository;
    private final org.enterprise.hr.repository.EmployeeRepository employeeRepository;
    private final org.enterprise.organization.repository.ZoneRepository zoneRepository;
    
    public AreaService(AreaRepository repository,
                       org.enterprise.hr.repository.EmployeeRepository employeeRepository,
                       org.enterprise.organization.repository.ZoneRepository zoneRepository) {
        super(repository);
        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.zoneRepository = zoneRepository;
    }

    @Transactional(readOnly = true)
    public Page<Area> search(String query, Pageable pageable) {
        return repository.findAll(AreaSpecification.searchByQuery(query), pageable);
    }

    @Override
    @Transactional
    public Area save(Area entity) {
        if (entity.getId() != null && repository.existsById(entity.getId())) {
            return update(entity.getId(), entity);
        }
        return create(entity);
    }

    @Transactional
    public Area create(Area entity) {
        resolveRelations(entity);
        if (entity.getCode() == null || entity.getCode().trim().isEmpty()) {
            repository.findTopByCodeStartingWithOrderByIdDesc("AREA-")
                .ifPresentOrElse(
                    lastArea -> {
                        String lastCode = lastArea.getCode();
                        try {
                            int seq = Integer.parseInt(lastCode.replace("AREA-", ""));
                            entity.setCode(String.format("AREA-%03d", seq + 1));
                        } catch (Exception e) {
                            entity.setCode("AREA-001");
                        }
                    },
                    () -> entity.setCode("AREA-001")
                );
        }
        return super.save(entity);
    }

    @Transactional
    public Area update(Long id, Area incoming) {
        Area existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Area not found with id: " + id));

        if (incoming.getCode() != null && !incoming.getCode().trim().isEmpty()) {
            existing.setCode(incoming.getCode().trim());
        }
        if (incoming.getName() != null) {
            existing.setName(incoming.getName().trim());
        }
        if (incoming.getActive() != null) {
            existing.setActive(incoming.getActive());
        }

        if (incoming.getAreaManager() != null && incoming.getAreaManager().getId() != null) {
            existing.setAreaManager(employeeRepository.findById(incoming.getAreaManager().getId()).orElse(null));
        } else {
            existing.setAreaManager(null);
        }

        if (incoming.getZone() != null && incoming.getZone().getId() != null) {
            existing.setZone(zoneRepository.findById(incoming.getZone().getId()).orElse(null));
        } else {
            existing.setZone(null);
        }

        return repository.save(existing);
    }

    private void resolveRelations(Area entity) {
        if (entity.getAreaManager() != null && entity.getAreaManager().getId() != null) {
            entity.setAreaManager(employeeRepository.findById(entity.getAreaManager().getId()).orElse(null));
        } else {
            entity.setAreaManager(null);
        }

        if (entity.getZone() != null && entity.getZone().getId() != null) {
            entity.setZone(zoneRepository.findById(entity.getZone().getId()).orElse(null));
        } else {
            entity.setZone(null);
        }
    }
}
