package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.ServiceRequest;
import org.enterprise.crm.service.repository.ServiceRequestRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
