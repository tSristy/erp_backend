package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.ServiceRequest;
import org.enterprise.crm.service.repository.ServiceRequestRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceRequestService extends BaseService<ServiceRequest, Long> {

    private final ServiceRequestRepository repository;

    public ServiceRequestService(ServiceRequestRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<ServiceRequest> findByRegisteredProductId(Long registeredProductId) {
        return repository.findByRegisteredProductId(registeredProductId);
    }

    @Override
    public ServiceRequest save(ServiceRequest entity) {
        if (entity.getRequestNumber() == null || entity.getRequestNumber().isEmpty()) {
            entity.setRequestNumber("SR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return super.save(entity);
    }
}
