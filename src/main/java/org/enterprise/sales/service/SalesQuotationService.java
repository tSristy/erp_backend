package org.enterprise.sales.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.sales.entity.SalesQuotation;
import org.enterprise.sales.repository.SalesQuotationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalesQuotationService {

    private final SalesQuotationRepository salesQuotationRepository;

    @Transactional
    public SalesQuotation save(SalesQuotation salesQuotation) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (salesQuotation.getCompanyId() == null) {
            salesQuotation.setCompanyId(companyId);
        }
        if (salesQuotation.getDetails() != null) {
            for (var detail : salesQuotation.getDetails()) {
                detail.setSalesQuotation(salesQuotation);
                if (detail.getCompanyId() == null) {
                    detail.setCompanyId(companyId);
                }
            }
        }
        return salesQuotationRepository.save(salesQuotation);
    }

    @Transactional
    public SalesQuotation updateStatus(Long quotationId, SalesQuotation.QuotationStatus status) {
        SalesQuotation quotation = salesQuotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Sales Quotation not found"));

        quotation.setStatus(status);
        return salesQuotationRepository.save(quotation);
    }

    public java.util.List<SalesQuotation> findAll() {
        return salesQuotationRepository.findAll();
    }

    public java.util.Optional<SalesQuotation> findById(Long id) {
        return salesQuotationRepository.findById(id);
    }

    @org.springframework.transaction.annotation.Transactional
    public void delete(Long id) {
        salesQuotationRepository.deleteById(id);
    }
}
