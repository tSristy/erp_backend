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
            }
        }
        ServicePartsRequisition existing = null;
        if (entity.getId() != null) {
            existing = repository.findById(entity.getId()).orElse(null);
        }

        Map<Long, BigDecimal> existingIssued = Map.of();
        if (existing != null && existing.getDetails() != null) {
            existingIssued = existing.getDetails().stream()
                    .filter(d -> d.getId() != null)
                    .collect(Collectors.toMap(
                            ServicePartsRequisitionDetail::getId,
                            d -> d.getQuantityIssued() != null ? d.getQuantityIssued() : BigDecimal.ZERO
                    ));
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
                BigDecimal oldIssued = existingIssued.getOrDefault(detail.getId(), BigDecimal.ZERO);
                BigDecimal newIssued = detail.getQuantityIssued() != null ? detail.getQuantityIssued() : BigDecimal.ZERO;
                BigDecimal delta = newIssued.subtract(oldIssued);

                if (delta.compareTo(BigDecimal.ZERO) > 0) {
                    if (saved.getWarehouse() == null) {
                        throw new RuntimeException("Warehouse must be selected to issue parts.");
                    }

                    // Get cost from inventory
                    StockBalanceResponse stockBalance = inventoryService.getStockBalance(
                            detail.getProduct().getId(), saved.getWarehouse().getId(), null);
                    
                    BigDecimal unitCost = stockBalance.getAverageCost() != null ? stockBalance.getAverageCost() : BigDecimal.ZERO;
                    BigDecimal totalCost = unitCost.multiply(delta);

                    // Issue stock
                    InventoryTransactionRequest req = new InventoryTransactionRequest();
                    req.setItemId(detail.getProduct().getId());
                    req.setWarehouseId(saved.getWarehouse().getId());
                    req.setTransactionType("SERVICE_ISSUE");
                    req.setDocumentType("PARTS_REQUISITION");
                    req.setDocumentId(saved.getId());
                    req.setQuantity(delta);
                    inventoryService.issueStock(req);

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

    public org.springframework.data.domain.Page<ServicePartsRequisition> searchServicePartsRequisitions(Long companyId, String searchTerm, org.springframework.data.domain.Pageable pageable) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            return repository.searchServicePartsRequisitions(companyId, searchTerm.trim(), pageable);
        }
        return repository.findAllByCompanyId(companyId, pageable);
    }

}