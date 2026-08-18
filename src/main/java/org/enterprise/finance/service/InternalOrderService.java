package org.enterprise.finance.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.finance.entity.InternalOrder;
import org.enterprise.finance.repository.InternalOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalOrderService {

    private final InternalOrderRepository internalOrderRepository;

    public Page<InternalOrder> searchInternalOrders(String search, int page, int size) {
        Long companyId = TenantContext.getCompanyId();
        Pageable pageable = PageRequest.of(page, size);
        return internalOrderRepository.searchByCompanyId(companyId, search == null ? "" : search, pageable);
    }

    public InternalOrder getInternalOrderById(Long id) {
        Long companyId = TenantContext.getCompanyId();
        return internalOrderRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new RuntimeException("Internal Order not found"));
    }

    @Transactional
    public InternalOrder createInternalOrder(InternalOrder internalOrder) {
        Long companyId = TenantContext.getCompanyId();
        
        if (internalOrder.getCode() != null) {
            internalOrderRepository.findByCodeAndCompanyId(internalOrder.getCode(), companyId)
                    .ifPresent(p -> {
                        throw new RuntimeException("Internal Order code already exists: " + internalOrder.getCode());
                    });
        }
        
        internalOrder.setCompanyId(companyId);
        return internalOrderRepository.save(internalOrder);
    }

    @Transactional
    public InternalOrder updateInternalOrder(Long id, InternalOrder details) {
        InternalOrder internalOrder = getInternalOrderById(id);
        
        if (details.getCode() != null && !details.getCode().equals(internalOrder.getCode())) {
            internalOrderRepository.findByCodeAndCompanyId(details.getCode(), TenantContext.getCompanyId())
                    .ifPresent(p -> {
                        throw new RuntimeException("Internal Order code already exists: " + details.getCode());
                    });
            internalOrder.setCode(details.getCode());
        }

        internalOrder.setName(details.getName());
        internalOrder.setBudget(details.getBudget());
        if (details.getActive() != null) {
            internalOrder.setActive(details.getActive());
        }

        return internalOrderRepository.save(internalOrder);
    }

    @Transactional
    public void deleteInternalOrder(Long id) {
        InternalOrder internalOrder = getInternalOrderById(id);
        internalOrderRepository.delete(internalOrder);
    }
}
