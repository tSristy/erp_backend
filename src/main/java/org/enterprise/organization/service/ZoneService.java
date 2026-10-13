package org.enterprise.organization.service;

import org.enterprise.inventory.service.BaseService;
import org.enterprise.organization.entity.Zone;
import org.enterprise.organization.repository.ZoneRepository;
import org.enterprise.organization.specification.ZoneSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ZoneService extends BaseService<Zone, Long> {

    private final ZoneRepository repository;
    private final org.enterprise.hr.repository.EmployeeRepository employeeRepository;

    public ZoneService(ZoneRepository repository, org.enterprise.hr.repository.EmployeeRepository employeeRepository) {
        super(repository);
        this.repository = repository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<Zone> search(String query, Pageable pageable) {
        return repository.findAll(ZoneSpecification.searchByQuery(query), pageable);
    }

    @Override
    @Transactional
    public Zone save(Zone entity) {
        if (entity.getId() != null && repository.existsById(entity.getId())) {
            return update(entity.getId(), entity);
        }
        return create(entity);
    }

    @Transactional
    public Zone create(Zone entity) {
        resolveRelations(entity);
        if (entity.getCode() == null || entity.getCode().trim().isEmpty()) {
            repository.findTopByCodeStartingWithOrderByIdDesc("ZONE-")
                .ifPresentOrElse(
                    lastZone -> {
                        String lastCode = lastZone.getCode();
                        try {
                            int seq = Integer.parseInt(lastCode.replace("ZONE-", ""));
                            entity.setCode(String.format("ZONE-%03d", seq + 1));
                        } catch (Exception e) {
                            entity.setCode("ZONE-001");
                        }
                    },
                    () -> entity.setCode("ZONE-001")
                );
        }
        return super.save(entity);
    }

    @Transactional
    public Zone update(Long id, Zone incoming) {
        Zone existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found with id: " + id));

        if (incoming.getCode() != null && !incoming.getCode().trim().isEmpty()) {
            existing.setCode(incoming.getCode().trim());
        }
        if (incoming.getName() != null) {
            existing.setName(incoming.getName().trim());
        }
        if (incoming.getActive() != null) {
            existing.setActive(incoming.getActive());
        }

        if (incoming.getRegionalManager() != null && incoming.getRegionalManager().getId() != null) {
            existing.setRegionalManager(employeeRepository.findById(incoming.getRegionalManager().getId()).orElse(null));
        } else {
            existing.setRegionalManager(null);
        }

        return repository.save(existing);
    }

    private void resolveRelations(Zone entity) {
        if (entity.getRegionalManager() != null && entity.getRegionalManager().getId() != null) {
            entity.setRegionalManager(employeeRepository.findById(entity.getRegionalManager().getId()).orElse(null));
        } else {
            entity.setRegionalManager(null);
        }
    }
}
