package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.ServicePartsRequisition;
import org.enterprise.crm.service.repository.ServicePartsRequisitionRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicePartsRequisitionService extends BaseService<ServicePartsRequisition, Long> {

    private final ServicePartsRequisitionRepository repository;

    public ServicePartsRequisitionService(ServicePartsRequisitionRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<ServicePartsRequisition> findByServiceRequestId(Long serviceRequestId) {
        return repository.findByServiceRequestId(serviceRequestId);
    }
}
