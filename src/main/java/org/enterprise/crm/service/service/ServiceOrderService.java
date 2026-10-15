package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.ServiceOrder;
import org.enterprise.crm.service.repository.ServiceOrderRepository;
import org.enterprise.crm.service.repository.ServiceRequestRepository;
import org.enterprise.inventory.service.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceOrderService extends BaseService<ServiceOrder, Long> {

    private final ServiceOrderRepository repository;
    private final ServiceRequestRepository serviceRequestRepository;

    public ServiceOrderService(
            ServiceOrderRepository repository,
            ServiceRequestRepository serviceRequestRepository) {
        super(repository);
        this.repository = repository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public List<ServiceOrder> findByServiceRequestId(Long serviceRequestId) {
        return repository.findByServiceRequestId(serviceRequestId);
    }

    public String generateNextOrderNumber() {
        return repository.findTopByOrderNumberStartingWithOrderByIdDesc("SO-")
            .map(lastOrder -> {
                try {
                    String raw = lastOrder.getOrderNumber();
                    String digits = raw.replaceAll("\\D+", "");
                    int seq = Integer.parseInt(digits);
                    return String.format("SO-%04d", seq + 1);
                } catch (Exception e) {
                    return "SO-" + String.format("%04d", lastOrder.getId() != null ? lastOrder.getId() + 1 : 1);
                }
            })
            .orElseGet(() -> {
                return repository.findTopByOrderByIdDesc()
                    .map(last -> String.format("SO-%04d", last.getId() + 1))
                    .orElse("SO-0001");
            });
    }

    @Transactional
    @Override
    public ServiceOrder save(ServiceOrder entity) {
        if (entity.getOrderNumber() == null || entity.getOrderNumber().trim().isEmpty()) {
            String nextNum = generateNextOrderNumber();
            while (repository.existsByOrderNumber(nextNum)) {
                try {
                    int seq = Integer.parseInt(nextNum.replaceAll("\\D+", ""));
                    nextNum = String.format("SO-%04d", seq + 1);
                } catch (Exception e) {
                    nextNum = "SO-" + (System.currentTimeMillis() % 100000);
                }
            }
            entity.setOrderNumber(nextNum);
        }

        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (companyId != null && entity.getCompanyId() == null) {
            entity.setCompanyId(companyId);
        }

        Long reqId = null;
        if (entity.getServiceRequest() != null && entity.getServiceRequest().getId() != null) {
            reqId = entity.getServiceRequest().getId();
        } else if (entity.getServiceRequestId() != null) {
            reqId = entity.getServiceRequestId();
        }

        if (reqId != null) {
            entity.setServiceRequest(serviceRequestRepository.findById(reqId).orElse(null));
        }

        return super.save(entity);
    }

    public Page<ServiceOrder> searchServiceOrders(Long companyId, String searchTerm, Pageable pageable) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            return repository.searchServiceOrders(companyId, searchTerm.trim(), pageable);
        }
        return repository.findAllByCompanyId(companyId, pageable);
    }

}