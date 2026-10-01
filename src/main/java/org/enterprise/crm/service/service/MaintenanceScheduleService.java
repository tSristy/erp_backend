package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.MaintenanceSchedule;
import org.enterprise.crm.service.repository.MaintenanceScheduleRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceScheduleService extends BaseService<MaintenanceSchedule, Long> {

    private final MaintenanceScheduleRepository repository;

    public MaintenanceScheduleService(MaintenanceScheduleRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<MaintenanceSchedule> findByRegisteredProductId(Long registeredProductId) {
        return repository.findByRegisteredProductId(registeredProductId);
    }

    public org.springframework.data.domain.Page<MaintenanceSchedule> searchMaintenanceSchedules(Long companyId, String searchTerm, org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(pageable);
    }

}