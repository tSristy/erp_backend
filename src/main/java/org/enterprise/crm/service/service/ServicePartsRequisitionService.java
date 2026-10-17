package org.enterprise.crm.service.service;

import org.enterprise.crm.service.entity.ServicePartsRequisition;
import org.enterprise.crm.service.entity.ServicePartsRequisitionDetail;
import org.enterprise.crm.service.repository.ServicePartsRequisitionRepository;
import org.enterprise.inventory.dto.InventoryTransactionRequest;
import org.enterprise.inventory.dto.StockBalanceResponse;
import org.enterprise.inventory.service.BaseService;
import org.enterprise.inventory.service.InventoryService;
import org.enterprise.finance.service.JournalEntryService;
import org.enterprise.finance.entity.JournalEntry;
import org.enterprise.finance.entity.JournalEntryLine;
import org.enterprise.finance.enums.JournalStatus;
import org.enterprise.crm.service.repository.ServiceOrderRepository;
import org.enterprise.inventory.repository.ProductRepository;
import org.enterprise.inventory.repository.ProductVariantRepository;
import org.enterprise.inventory.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServicePartsRequisitionService extends BaseService<ServicePartsRequisition, Long> {

    private final ServicePartsRequisitionRepository repository;
    private final InventoryService inventoryService;
    private final JournalEntryService journalEntryService;
    private final WarehouseRepository warehouseRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public ServicePartsRequisitionService(ServicePartsRequisitionRepository repository,
                                          InventoryService inventoryService,
                                          JournalEntryService journalEntryService,
                                          WarehouseRepository warehouseRepository,
                                          ServiceOrderRepository serviceOrderRepository,
                                          ProductRepository productRepository,
                                          ProductVariantRepository productVariantRepository) {
        super(repository);
        this.repository = repository;
        this.inventoryService = inventoryService;
        this.journalEntryService = journalEntryService;
        this.warehouseRepository = warehouseRepository;
        this.serviceOrderRepository = serviceOrderRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public List<ServicePartsRequisition> findByServiceOrderId(Long serviceOrderId) {
        return repository.findByServiceOrderId(serviceOrderId);
    }

    @Override
    @Transactional
    public ServicePartsRequisition save(ServicePartsRequisition entity) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (companyId != null && entity.getCompanyId() == null) {
            entity.setCompanyId(companyId);
        }

        ServicePartsRequisition existing = null;
        if (entity.getId() != null) {
            existing = repository.findById(entity.getId())
                    .orElseThrow(() -> new RuntimeException("Service parts requisition not found: " + entity.getId()));

            if (existing.getStatus() == ServicePartsRequisition.RequisitionStatus.CANCELLED) {
                throw new RuntimeException("Cannot modify a CANCELLED parts requisition.");
            }
            if (existing.getStatus() == ServicePartsRequisition.RequisitionStatus.ISSUED
                    && entity.getStatus() != ServicePartsRequisition.RequisitionStatus.CANCELLED) {
                throw new RuntimeException("Cannot modify an ISSUED parts requisition. Only cancellation is allowed.");
            }
            if (entity.getCompanyId() == null) {
                entity.setCompanyId(existing.getCompanyId());
            }
        }

        Map<Long, BigDecimal> existingIssuedById = new java.util.HashMap<>();
        Map<String, BigDecimal> existingIssuedByProductKey = new java.util.HashMap<>();
        List<ServicePartsRequisitionDetail> snapshotExistingDetails = new ArrayList<>();
        Long existingWarehouseId = existing != null && existing.getWarehouse() != null ? existing.getWarehouse().getId() : null;

        if (existing != null && existing.getDetails() != null) {
            for (ServicePartsRequisitionDetail d : existing.getDetails()) {
                BigDecimal issued = d.getQuantityIssued() != null ? d.getQuantityIssued() : BigDecimal.ZERO;
                if (d.getId() != null) {
                    existingIssuedById.put(d.getId(), issued);
                }
                Long pId = d.getProduct() != null ? d.getProduct().getId() : d.getProductId();
                Long vId = d.getVariant() != null ? d.getVariant().getId() : d.getVariantId();
                if (pId != null) {
                    String key = pId + ":" + (vId != null ? vId : "0");
                    existingIssuedByProductKey.merge(key, issued, BigDecimal::add);
                }
                ServicePartsRequisitionDetail copy = new ServicePartsRequisitionDetail();
                copy.setId(d.getId());
                copy.setProduct(d.getProduct());
                copy.setVariant(d.getVariant());
                copy.setQuantityRequested(d.getQuantityRequested());
                copy.setQuantityIssued(issued);
                snapshotExistingDetails.add(copy);
            }
        }

        boolean hadIssuedStock = snapshotExistingDetails.stream()
                .anyMatch(d -> d.getQuantityIssued() != null && d.getQuantityIssued().compareTo(BigDecimal.ZERO) > 0);

        Long soId = null;
        if (entity.getServiceOrder() != null && entity.getServiceOrder().getId() != null) {
            soId = entity.getServiceOrder().getId();
        } else if (entity.getServiceOrderId() != null) {
            soId = entity.getServiceOrderId();
        }
        if (soId != null) {
            entity.setServiceOrder(serviceOrderRepository.findById(soId).orElse(null));
        }

        Long whId = null;
        if (entity.getWarehouse() != null && entity.getWarehouse().getId() != null) {
            whId = entity.getWarehouse().getId();
        } else if (entity.getWarehouseId() != null) {
            whId = entity.getWarehouseId();
        }
        if (hadIssuedStock && existingWarehouseId != null && whId != null && !existingWarehouseId.equals(whId)) {
            throw new RuntimeException("Cannot change warehouse after parts have already been issued.");
        }
        if (whId != null) {
            entity.setWarehouse(warehouseRepository.findById(whId).orElse(null));
        }

        if (entity.getDetails() != null) {
            for (ServicePartsRequisitionDetail detail : entity.getDetails()) {
                detail.setRequisition(entity);
                if (companyId != null && detail.getCompanyId() == null) {
                    detail.setCompanyId(companyId);
                }

                Long prodId = null;
                if (detail.getProduct() != null && detail.getProduct().getId() != null) {
                    prodId = detail.getProduct().getId();
                } else if (detail.getProductId() != null) {
                    prodId = detail.getProductId();
                }
                if (prodId != null) {
                    detail.setProduct(productRepository.findById(prodId).orElse(null));
                }

                Long varId = null;
                if (detail.getVariant() != null && detail.getVariant().getId() != null) {
                    varId = detail.getVariant().getId();
                } else if (detail.getVariantId() != null) {
                    varId = detail.getVariantId();
                }
                if (varId != null) {
                    detail.setVariant(productVariantRepository.findById(varId).orElse(null));
                }

                if (entity.getStatus() == ServicePartsRequisition.RequisitionStatus.CANCELLED) {
                    detail.setQuantityIssued(BigDecimal.ZERO);
                }
            }
        }

        // If transitioning to CANCELLED and parts were previously issued, return stock and reverse GL
        if (existing != null
                && entity.getStatus() == ServicePartsRequisition.RequisitionStatus.CANCELLED
                && hadIssuedStock) {
            Long targetWarehouseId = existingWarehouseId != null
                    ? existingWarehouseId
                    : (entity.getWarehouse() != null ? entity.getWarehouse().getId() : null);
            if (targetWarehouseId == null) {
                throw new RuntimeException("Warehouse is required to reverse issued parts on cancellation.");
            }

            for (ServicePartsRequisitionDetail oldDetail : snapshotExistingDetails) {
                BigDecimal issuedQty = oldDetail.getQuantityIssued() != null ? oldDetail.getQuantityIssued() : BigDecimal.ZERO;
                if (issuedQty.compareTo(BigDecimal.ZERO) > 0 && oldDetail.getProduct() != null) {
                    StockBalanceResponse stockBalance = inventoryService.getStockBalance(
                            oldDetail.getProduct().getId(), targetWarehouseId, null);
                    BigDecimal unitCost = stockBalance.getAverageCost() != null ? stockBalance.getAverageCost() : BigDecimal.ZERO;

                    InventoryTransactionRequest returnReq = new InventoryTransactionRequest();
                    returnReq.setItemId(oldDetail.getProduct().getId());
                    returnReq.setVariantId(oldDetail.getVariant() != null ? oldDetail.getVariant().getId() : null);
                    returnReq.setWarehouseId(targetWarehouseId);
                    returnReq.setTransactionType("SERVICE_RETURN");
                    returnReq.setDocumentType("PARTS_REQUISITION_CANCEL");
                    returnReq.setDocumentId(existing.getId());
                    returnReq.setQuantity(issuedQty);
                    returnReq.setUnitCost(unitCost);
                    inventoryService.receiveStock(returnReq);
                }
            }

            journalEntryService.reverseByReference("SERVICE_PARTS_REQUISITION", existing.getId());
        }

        ServicePartsRequisition saved = super.save(entity);

        if (saved.getStatus() == ServicePartsRequisition.RequisitionStatus.ISSUED ||
            saved.getStatus() == ServicePartsRequisition.RequisitionStatus.PARTIALLY_ISSUED) {

            JournalEntry journalEntry = new JournalEntry();
            journalEntry.setReferenceType("SERVICE_PARTS_REQUISITION");
            journalEntry.setReferenceId(saved.getId());
            journalEntry.setStatus(JournalStatus.POSTED);
            journalEntry.setLines(new ArrayList<>());

            boolean hasJournalEntries = false;

            for (ServicePartsRequisitionDetail detail : saved.getDetails()) {
                Long pId = detail.getProduct() != null ? detail.getProduct().getId() : detail.getProductId();
                Long vId = detail.getVariant() != null ? detail.getVariant().getId() : detail.getVariantId();
                String key = (pId != null ? pId : "0") + ":" + (vId != null ? vId : "0");

                BigDecimal oldIssued = detail.getId() != null && existingIssuedById.containsKey(detail.getId())
                        ? existingIssuedById.get(detail.getId())
                        : existingIssuedByProductKey.getOrDefault(key, BigDecimal.ZERO);
                BigDecimal newIssued = detail.getQuantityIssued() != null ? detail.getQuantityIssued() : BigDecimal.ZERO;
                BigDecimal delta = newIssued.subtract(oldIssued);

                if (delta.compareTo(BigDecimal.ZERO) < 0) {
                    throw new RuntimeException("Cannot reduce already issued quantity for product. Cancel the requisition to reverse issued parts.");
                }

                if (delta.compareTo(BigDecimal.ZERO) > 0) {
                    if (saved.getWarehouse() == null) {
                        throw new RuntimeException("Warehouse must be selected to issue parts.");
                    }

                    StockBalanceResponse stockBalance = inventoryService.getStockBalance(
                            detail.getProduct().getId(), saved.getWarehouse().getId(), null);

                    BigDecimal unitCost = stockBalance.getAverageCost() != null ? stockBalance.getAverageCost() : BigDecimal.ZERO;
                    BigDecimal fallbackCost = unitCost.multiply(delta);

                    // Issue stock
                    InventoryTransactionRequest req = new InventoryTransactionRequest();
                    req.setItemId(detail.getProduct().getId());
                    req.setVariantId(detail.getVariant() != null ? detail.getVariant().getId() : null);
                    req.setWarehouseId(saved.getWarehouse().getId());
                    req.setTransactionType("SERVICE_ISSUE");
                    req.setDocumentType("PARTS_REQUISITION");
                    req.setDocumentId(saved.getId());
                    req.setQuantity(delta);
                    BigDecimal issuedValue = inventoryService.issueStock(req);
                    BigDecimal totalCost = (issuedValue != null && issuedValue.compareTo(BigDecimal.ZERO) > 0)
                            ? issuedValue
                            : fallbackCost;

                    // Accounting Lines
                    if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
                        hasJournalEntries = true;

                        // Credit Inventory Asset
                        if (detail.getProduct().getInventoryAccount() != null) {
                            JournalEntryLine creditLine = new JournalEntryLine();
                            creditLine.setAccount(detail.getProduct().getInventoryAccount());
                            creditLine.setCredit(totalCost);
                            creditLine.setDebit(BigDecimal.ZERO);
                            creditLine.setJournalEntry(journalEntry);
                            journalEntry.getLines().add(creditLine);
                        } else {
                            throw new RuntimeException("Inventory Account not configured for product: " + detail.getProduct().getName());
                        }

                        // Debit COGS / Service Expense
                        if (detail.getProduct().getCogsAccount() != null) {
                            JournalEntryLine debitLine = new JournalEntryLine();
                            debitLine.setAccount(detail.getProduct().getCogsAccount());
                            debitLine.setDebit(totalCost);
                            debitLine.setCredit(BigDecimal.ZERO);
                            debitLine.setJournalEntry(journalEntry);
                            journalEntry.getLines().add(debitLine);
                        } else {
                            throw new RuntimeException("COGS/Expense Account not configured for product: " + detail.getProduct().getName());
                        }
                    }
                }
            }

            if (hasJournalEntries) {
                journalEntryService.save(journalEntry);
            }
        }

        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ServicePartsRequisition existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service parts requisition not found: " + id));
        if (existing.getStatus() != ServicePartsRequisition.RequisitionStatus.REQUESTED) {
            throw new RuntimeException("Only REQUESTED parts requisitions can be deleted. Current status: " + existing.getStatus());
        }
        boolean anyIssued = existing.getDetails() != null && existing.getDetails().stream()
                .anyMatch(d -> d.getQuantityIssued() != null && d.getQuantityIssued().compareTo(BigDecimal.ZERO) > 0);
        if (anyIssued) {
            throw new RuntimeException("Cannot delete a parts requisition that has issued stock.");
        }
        super.delete(id);
    }

    public org.springframework.data.domain.Page<ServicePartsRequisition> searchServicePartsRequisitions(Long companyId, String searchTerm, org.springframework.data.domain.Pageable pageable) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            return repository.searchServicePartsRequisitions(companyId, searchTerm.trim(), pageable);
        }
        return repository.findAllByCompanyId(companyId, pageable);
    }

}