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

    @Transactional(readOnly = true)
    public Page<Zone> search(String query, Pageable pageable) {
        return repository.findAll(ZoneSpecification.searchByQuery(query), pageable);
    }

    private final org.enterprise.hr.repository.EmployeeRepository employeeRepository;

    public ZoneService(ZoneRepository repository, org.enterprise.hr.repository.EmployeeRepository employeeRepository) {
        super(repository);
        this.repository = repository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public Zone save(Zone entity) {
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
}
