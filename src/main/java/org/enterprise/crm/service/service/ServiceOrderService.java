package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.ServiceOrder;
import org.enterprise.crm.service.repository.ServiceOrderRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceOrderService extends BaseService<ServiceOrder, Long> {

    private final ServiceOrderRepository repository;

    public ServiceOrderService(ServiceOrderRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<ServiceOrder> findByServiceRequestId(Long serviceRequestId) {
        return repository.findByServiceRequestId(serviceRequestId);
    }
}
