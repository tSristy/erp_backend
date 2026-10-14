package org.enterprise.sales.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.sales.entity.SalesOrder;
import org.enterprise.sales.entity.SalesOrderDetail;
import org.enterprise.sales.repository.SalesOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final org.enterprise.inventory.repository.BusinessPartnerRepository businessPartnerRepository;
    private final org.enterprise.inventory.repository.WarehouseRepository warehouseRepository;
    private final org.enterprise.inventory.repository.ProductRepository productRepository;
    private final org.enterprise.inventory.repository.ProductVariantRepository productVariantRepository;
    private final org.enterprise.sales.repository.SalesQuotationRepository salesQuotationRepository;
    private final org.enterprise.sales.mapper.SalesMapper mapper;

    public synchronized String generateNextOrderNo(Long companyId) {
        long nextNum = salesOrderRepository.findTopByOrderByIdDesc()
                .map(so -> so.getId() + 1)
                .orElse(1L);
        String candidate = String.format("SO-%05d", nextNum);
        while (salesOrderRepository.existsByOrderNo(candidate)) {
            nextNum++;
            candidate = String.format("SO-%05d", nextNum);
        }
        return candidate;
    }

    @Transactional
    public SalesOrder save(SalesOrder salesOrder) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        Long userId = org.enterprise.common.util.TenantContext.getUserId();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (salesOrder.getCompanyId() == null) {
            salesOrder.setCompanyId(companyId);
        }
        if (salesOrder.getCreatedAt() == null) {
            salesOrder.setCreatedAt(now);
        }
        if (salesOrder.getUpdatedAt() == null) {
            salesOrder.setUpdatedAt(now);
        }
        if (salesOrder.getCreatedBy() == null) {
            salesOrder.setCreatedBy(userId);
        }
        if (salesOrder.getUpdatedBy() == null) {
            salesOrder.setUpdatedBy(userId);
        }
        if (salesOrder.getOrderNo() == null || salesOrder.getOrderNo().trim().isEmpty()) {
            salesOrder.setOrderNo(generateNextOrderNo(companyId));
        }
        if (salesOrder.getCustomer() != null && salesOrder.getCustomer().getId() != null) {
            salesOrder.setCustomer(businessPartnerRepository.findById(salesOrder.getCustomer().getId()).orElse(null));
        } else {
            salesOrder.setCustomer(null);
        }
        if (salesOrder.getWarehouse() != null && salesOrder.getWarehouse().getId() != null) {
            salesOrder.setWarehouse(warehouseRepository.findById(salesOrder.getWarehouse().getId()).orElse(null));
        } else {
            salesOrder.setWarehouse(null);
        }
        if (salesOrder.getSalesQuotation() != null && salesOrder.getSalesQuotation().getId() != null) {
            salesOrder.setSalesQuotation(salesQuotationRepository.findById(salesOrder.getSalesQuotation().getId()).orElse(null));
        } else {
            salesOrder.setSalesQuotation(null);
        }
        if (salesOrder.getReferenceOrder() != null && salesOrder.getReferenceOrder().getId() != null) {
            salesOrder.setReferenceOrder(salesOrderRepository.findById(salesOrder.getReferenceOrder().getId()).orElse(null));
        } else {
            salesOrder.setReferenceOrder(null);
        }
        if (salesOrder.getDiscounts() != null) {
            for (var discount : salesOrder.getDiscounts()) {
                discount.setSalesOrder(salesOrder);
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
        if (salesOrder.getDetails() != null) {
            for (var detail : salesOrder.getDetails()) {
                detail.setSalesOrder(salesOrder);
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
                        dd.setSalesOrderDetail(detail);
                    }
                }
            }
        }
        return salesOrderRepository.save(salesOrder);
    }

    @Transactional
    public SalesOrder update(Long id, SalesOrder updated) {
        SalesOrder existing = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales Order not found with id: " + id));

        Long userId = org.enterprise.common.util.TenantContext.getUserId();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        existing.setUpdatedAt(now);
        if (userId != null) {
            existing.setUpdatedBy(userId);
        }

        if (updated.getOrderNo() != null && !updated.getOrderNo().trim().isEmpty()) {
            existing.setOrderNo(updated.getOrderNo());
        }
        existing.setOrderDate(updated.getOrderDate());
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }
        if (updated.getOrderType() != null) {
            existing.setOrderType(updated.getOrderType());
        }
        if (updated.getSalesChannel() != null) {
            existing.setSalesChannel(updated.getSalesChannel());
        }
        existing.setSubTotal(updated.getSubTotal());
        existing.setDiscountTotal(updated.getDiscountTotal());
        existing.setVatTotal(updated.getVatTotal());
        existing.setTotalAmount(updated.getTotalAmount());
        existing.setRemarks(updated.getRemarks());

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
        if (updated.getSalesQuotation() != null && updated.getSalesQuotation().getId() != null) {
            existing.setSalesQuotation(salesQuotationRepository.findById(updated.getSalesQuotation().getId()).orElse(null));
        } else {
            existing.setSalesQuotation(null);
        }
        if (updated.getReferenceOrder() != null && updated.getReferenceOrder().getId() != null) {
            existing.setReferenceOrder(salesOrderRepository.findById(updated.getReferenceOrder().getId()).orElse(null));
        } else {
            existing.setReferenceOrder(null);
        }

        // Update discounts
        if (existing.getDiscounts() != null) {
            existing.getDiscounts().clear();
        } else {
            existing.setDiscounts(new ArrayList<>());
        }
        if (updated.getDiscounts() != null) {
            for (var discount : updated.getDiscounts()) {
                discount.setSalesOrder(existing);
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
            existing.setDetails(new ArrayList<>());
        }
        if (updated.getDetails() != null) {
            for (var d : updated.getDetails()) {
                d.setSalesOrder(existing);
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
                        dd.setSalesOrderDetail(d);
                    }
                }
                existing.getDetails().add(d);
            }
        }

        return salesOrderRepository.save(existing);
    }

    @Transactional
    public SalesOrder confirmOrder(Long salesOrderId) {
        SalesOrder order = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new RuntimeException("Sales Order not found"));

        if (order.getStatus() != SalesOrder.SalesOrderStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT orders can be confirmed");
        }
        order.setStatus(SalesOrder.SalesOrderStatus.CONFIRMED);
        return salesOrderRepository.save(order);
    }

    @Transactional
    public SalesOrder createReturn(Long originalOrderId) {
        SalesOrder original = salesOrderRepository.findById(originalOrderId)
                .orElseThrow(() -> new RuntimeException("Original order not found"));

        SalesOrder returnOrder = new SalesOrder();
        returnOrder.setOrderType(SalesOrder.OrderType.RETURN);
        returnOrder.setReferenceOrder(original);
        returnOrder.setCustomer(original.getCustomer());
        returnOrder.setWarehouse(original.getWarehouse());
        returnOrder.setSalesChannel(original.getSalesChannel());
        returnOrder.setStatus(SalesOrder.SalesOrderStatus.DRAFT);
        returnOrder.setOrderDate(LocalDate.now());
        returnOrder.setRemarks("Return for order " + (original.getOrderNo() != null ? original.getOrderNo() : original.getId()));

        List<SalesOrderDetail> returnDetails = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (SalesOrderDetail originalDetail : original.getDetails()) {
            BigDecimal returnableQty = originalDetail.getShippedQuantity().subtract(originalDetail.getReturnedQuantity());
            if (returnableQty.compareTo(BigDecimal.ZERO) > 0) {
                SalesOrderDetail returnDetail = new SalesOrderDetail();
                returnDetail.setSalesOrder(returnOrder);
                returnDetail.setProduct(originalDetail.getProduct());
                returnDetail.setVariant(originalDetail.getVariant());
                returnDetail.setQuantity(returnableQty);
                returnDetail.setUnitPrice(originalDetail.getUnitPrice());
                
                BigDecimal lineTotal = returnableQty.multiply(originalDetail.getUnitPrice());
                returnDetail.setLineTotal(lineTotal);
                totalAmount = totalAmount.add(lineTotal);
                
                returnDetails.add(returnDetail);
            }
        }        
        returnOrder.setDetails(returnDetails);
        returnOrder.setTotalAmount(totalAmount);

        return save(returnOrder);
    }

    public List<SalesOrder> findAll() {
        return salesOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<SalesOrder> findAll(org.springframework.data.domain.Pageable pageable) {
        return salesOrderRepository.findAll(pageable);
    }

    public java.util.Optional<SalesOrder> findById(Long id) {
        return salesOrderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<org.enterprise.sales.dto.SalesOrderDto> findDtoById(Long id) {
        return salesOrderRepository.findById(id).map(entity -> {
            if (entity.getDetails() != null) {
                for (var detail : entity.getDetails()) {
                    if (detail.getVariant() != null) {
                        detail.getVariant().getId();
                        detail.getVariant().getSku();
                    }
                }
            }
            org.enterprise.sales.dto.SalesOrderDto dto = mapper.toDto(entity);
            if (entity.getCustomer() != null) {
                dto.setCustomerName(entity.getCustomer().getName());
            }
            if (entity.getWarehouse() != null) {
                dto.setWarehouseName(entity.getWarehouse().getName());
            }
            if (entity.getSalesQuotation() != null) {
                dto.setSalesQuotationNo(entity.getSalesQuotation().getQuotationNo());
            }
            return dto;
        });
    }

    @Transactional
    public void delete(Long id) {
        salesOrderRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<SalesOrder> search(String query, org.springframework.data.domain.Pageable pageable) {
        return salesOrderRepository.findAll(org.enterprise.sales.specification.SalesOrderSpecification.searchByQuery(query), pageable);
    }
}