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
        return salesQuotationRepository.save(salesQuotation);
    }

    @Transactional
    public SalesQuotation updateStatus(Long quotationId, SalesQuotation.QuotationStatus status) {
        SalesQuotation quotation = salesQuotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Sales Quotation not found"));

        quotation.setStatus(status);
        return salesQuotationRepository.save(quotation);
    }
}
