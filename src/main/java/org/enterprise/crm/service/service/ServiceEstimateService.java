package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.ServiceEstimate;
import org.enterprise.crm.service.repository.ServiceEstimateRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceEstimateService extends BaseService<ServiceEstimate, Long> {

    private final ServiceEstimateRepository repository;

    public ServiceEstimateService(ServiceEstimateRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<ServiceEstimate> findByServiceRequestId(Long serviceRequestId) {
        return repository.findByServiceRequestId(serviceRequestId);
    }
}
