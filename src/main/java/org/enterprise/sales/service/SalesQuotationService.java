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
    private final org.enterprise.inventory.repository.BusinessPartnerRepository businessPartnerRepository;
    private final org.enterprise.inventory.repository.WarehouseRepository warehouseRepository;
    private final org.enterprise.inventory.repository.ProductRepository productRepository;
    private final org.enterprise.inventory.repository.ProductVariantRepository productVariantRepository;
    private final org.enterprise.sales.mapper.SalesMapper mapper;

    public synchronized String generateNextQuotationNo(Long companyId) {
        long nextNum = salesQuotationRepository.findTopByOrderByIdDesc()
                .map(sq -> sq.getId() + 1)
                .orElse(1L);
        String candidate = String.format("SQ-%05d", nextNum);
        while (salesQuotationRepository.existsByQuotationNo(candidate)) {
            nextNum++;
            candidate = String.format("SQ-%05d", nextNum);
        }
        return candidate;
    }

    @Transactional
    public SalesQuotation save(SalesQuotation salesQuotation) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        Long userId = org.enterprise.common.util.TenantContext.getUserId();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (salesQuotation.getCompanyId() == null) {
            salesQuotation.setCompanyId(companyId);
        }
        if (salesQuotation.getCreatedAt() == null) {
            salesQuotation.setCreatedAt(now);
        }
        if (salesQuotation.getUpdatedAt() == null) {
            salesQuotation.setUpdatedAt(now);
        }
        if (salesQuotation.getCreatedBy() == null) {
            salesQuotation.setCreatedBy(userId);
        }
        if (salesQuotation.getUpdatedBy() == null) {
            salesQuotation.setUpdatedBy(userId);
        }
        if (salesQuotation.getQuotationNo() == null || salesQuotation.getQuotationNo().trim().isEmpty()) {
            salesQuotation.setQuotationNo(generateNextQuotationNo(companyId));
        }
        if (salesQuotation.getCustomer() != null && salesQuotation.getCustomer().getId() != null) {
            salesQuotation.setCustomer(businessPartnerRepository.findById(salesQuotation.getCustomer().getId()).orElse(null));
        } else {
            salesQuotation.setCustomer(null);
        }
        if (salesQuotation.getWarehouse() != null && salesQuotation.getWarehouse().getId() != null) {
            salesQuotation.setWarehouse(warehouseRepository.findById(salesQuotation.getWarehouse().getId()).orElse(null));
        } else {
            salesQuotation.setWarehouse(null);
        }
        if (salesQuotation.getDiscounts() != null) {
            for (var discount : salesQuotation.getDiscounts()) {
                discount.setSalesQuotation(salesQuotation);
                if (discount.getCompanyId() == null) {
                    discount.setCompanyId(companyId);
                }
                if (discount.getCreatedAt() == null) {
                    discount.setCreatedAt(now);
                }
                if (discount.getUpdatedAt() == null) {
                    discount.setUpdatedAt(now);
                }
                if (discount.getCreatedBy() == null) {
                    discount.setCreatedBy(userId);
                }
                if (discount.getUpdatedBy() == null) {
                    discount.setUpdatedBy(userId);
                }
            }
        }
        if (salesQuotation.getDetails() != null) {
            for (var detail : salesQuotation.getDetails()) {
                detail.setSalesQuotation(salesQuotation);
                if (detail.getCompanyId() == null) {
                    detail.setCompanyId(companyId);
                }
                if (detail.getCreatedAt() == null) {
                    detail.setCreatedAt(now);
                }
                if (detail.getUpdatedAt() == null) {
                    detail.setUpdatedAt(now);
                }
                if (detail.getCreatedBy() == null) {
                    detail.setCreatedBy(userId);
                }
                if (detail.getUpdatedBy() == null) {
                    detail.setUpdatedBy(userId);
                }
                if (detail.getProduct() != null && detail.getProduct().getId() != null) {
                    detail.setProduct(productRepository.findById(detail.getProduct().getId()).orElse(null));
                } else {
                    detail.setProduct(null);
                }
                Long varId = (detail.getVariant() != null && detail.getVariant().getId() != null)
                        ? detail.getVariant().getId() : null;
                if (varId != null) {
                    detail.setVariant(productVariantRepository.findById(varId).orElse(null));
                } else {
                    detail.setVariant(null);
                }
                if (detail.getDiscounts() != null) {
                    for (var dd : detail.getDiscounts()) {
                        dd.setSalesQuotationDetail(detail);
                    }
                }
            }
        }
        return salesQuotationRepository.save(salesQuotation);
    }

    @Transactional
    public SalesQuotation update(Long id, SalesQuotation updated) {
        SalesQuotation existing = salesQuotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales Quotation not found with id: " + id));

        Long userId = org.enterprise.common.util.TenantContext.getUserId();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        existing.setUpdatedAt(now);
        if (userId != null) {
            existing.setUpdatedBy(userId);
        }

        if (updated.getQuotationNo() != null && !updated.getQuotationNo().trim().isEmpty()) {
            existing.setQuotationNo(updated.getQuotationNo());
        }
        existing.setQuotationDate(updated.getQuotationDate());
        existing.setValidUntil(updated.getValidUntil());
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }
        if (updated.getSalesChannel() != null) {
            existing.setSalesChannel(updated.getSalesChannel());
        }
        existing.setSubTotal(updated.getSubTotal());
        existing.setDiscountTotal(updated.getDiscountTotal());
        existing.setTotalAmount(updated.getTotalAmount());

        if (updated.getCustomer() != null && updated.getCustomer().getId() != null) {
            existing.setCustomer(businessPartnerRepository.findById(updated.getCustomer().getId()).orElse(null));
        } else {
            existing.setCustomer(null);
        }
        if (updated.getWarehouse() != null && updated.getWarehouse().getId() != null) {
            existing.setWarehouse(warehouseRepository.findById(updated.getWarehouse().getId()).orElse(null));
        } else {
            existing.setWarehouse(null);
        }

        // Update discounts
        if (existing.getDiscounts() != null) {
            existing.getDiscounts().clear();
        } else {
            existing.setDiscounts(new java.util.ArrayList<>());
        }
        if (updated.getDiscounts() != null) {
            for (var discount : updated.getDiscounts()) {
                discount.setSalesQuotation(existing);
                discount.setCompanyId(existing.getCompanyId());
                if (discount.getCreatedAt() == null) {
                    discount.setCreatedAt(now);
                }
                discount.setUpdatedAt(now);
                if (discount.getCreatedBy() == null) {
                    discount.setCreatedBy(userId);
                }
                if (userId != null) {
                    discount.setUpdatedBy(userId);
                }
                existing.getDiscounts().add(discount);
            }
        }

        // Update details
        if (existing.getDetails() != null) {
            existing.getDetails().clear();
        } else {
            existing.setDetails(new java.util.ArrayList<>());
        }
        if (updated.getDetails() != null) {
            for (var d : updated.getDetails()) {
                d.setSalesQuotation(existing);
                d.setCompanyId(existing.getCompanyId());
                if (d.getCreatedAt() == null) {
                    d.setCreatedAt(now);
                }
                d.setUpdatedAt(now);
                if (d.getCreatedBy() == null) {
                    d.setCreatedBy(userId);
                }
                if (userId != null) {
                    d.setUpdatedBy(userId);
                }
                if (d.getProduct() != null && d.getProduct().getId() != null) {
                    d.setProduct(productRepository.findById(d.getProduct().getId()).orElse(null));
                } else {
                    d.setProduct(null);
                }
                Long varId = (d.getVariant() != null && d.getVariant().getId() != null)
                        ? d.getVariant().getId() : null;
                if (varId != null) {
                    d.setVariant(productVariantRepository.findById(varId).orElse(null));
                } else {
                    d.setVariant(null);
                }
                if (d.getDiscounts() != null) {
                    for (var dd : d.getDiscounts()) {
                        dd.setSalesQuotationDetail(d);
                    }
                }
                existing.getDetails().add(d);
            }
        }

        return salesQuotationRepository.save(existing);
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

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<SalesQuotation> findAll(org.springframework.data.domain.Pageable pageable) {
        return salesQuotationRepository.findAll(pageable);
    }

    @org.springframework.transaction.annotation.Transactional
    public java.util.Optional<SalesQuotation> findById(Long id) {
        return salesQuotationRepository.findById(id).map(sq -> {
            if (sq.getQuotationNo() == null || sq.getQuotationNo().trim().isEmpty()) {
                sq.setQuotationNo(String.format("SQ-%05d", sq.getId()));
                sq = salesQuotationRepository.save(sq);
            }
            if (sq.getDetails() != null) {
                for (var d : sq.getDetails()) {
                    if (d.getVariant() != null) {
                        d.getVariant().getId();
                        d.getVariant().getSku();
                    }
                }
            }
            return sq;
        });
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.Optional<org.enterprise.sales.dto.SalesQuotationDto> findDtoById(Long id) {
        return findById(id).map(mapper::toDto);
    }

    @org.springframework.transaction.annotation.Transactional
    public void delete(Long id) {
        salesQuotationRepository.deleteById(id);
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<SalesQuotation> search(String query, org.springframework.data.domain.Pageable pageable) {
        return salesQuotationRepository.findAll(org.enterprise.sales.specification.SalesQuotationSpecification.searchByQuery(query), pageable);
    }
}