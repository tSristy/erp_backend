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
    
    public AreaService(AreaRepository repository, org.enterprise.hr.repository.EmployeeRepository employeeRepository) {
        super(repository);
        this.repository = repository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<Area> search(String query, Pageable pageable) {
        return repository.findAll(AreaSpecification.searchByQuery(query), pageable);
    }

    @Override
    @Transactional
    public Area save(Area entity) {
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
}
