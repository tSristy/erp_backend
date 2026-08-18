package org.enterprise.inventory.service;

import org.enterprise.inventory.dto.*;
import org.enterprise.inventory.entity.*;
import org.enterprise.inventory.enums.PurchaseOrderStatus;
import org.enterprise.inventory.repository.*;
import org.enterprise.workflow.dto.WorkflowStartRequest;
import org.enterprise.workflow.entity.WorkflowInstance;
import org.enterprise.workflow.service.WorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.event.EventListener;
import org.enterprise.workflow.event.WorkflowStatusEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseOrderService extends BaseService<PurchaseOrder, Long> {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final BusinessPartnerRepository businessPartnerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final CostHeadRepository costHeadRepository;
    private final LetterOfCreditRepository letterOfCreditRepository;
    private final WorkflowService workflowService;
    private final InventoryService inventoryService;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                BusinessPartnerRepository businessPartnerRepository,
                                WarehouseRepository warehouseRepository,
                                ProductRepository productRepository,
                                CostHeadRepository costHeadRepository,
                                LetterOfCreditRepository letterOfCreditRepository,
                                WorkflowService workflowService,
                                InventoryService inventoryService) {
        super(purchaseOrderRepository);
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.businessPartnerRepository = businessPartnerRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.costHeadRepository = costHeadRepository;
        this.letterOfCreditRepository = letterOfCreditRepository;
        this.workflowService = workflowService;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional
    public PurchaseOrder save(PurchaseOrder po) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (po.getCompanyId() == null) {
            po.setCompanyId(companyId);
        }
        
        if (po.getDetails() != null) {
            for (PurchaseOrderDetail detail : po.getDetails()) {
                if (detail.getCompanyId() == null) {
                    detail.setCompanyId(companyId);
                }
                detail.setPurchaseOrder(po);
                
                if (detail.getCosts() != null) {
                    for (PurchaseOrderDetailCost cost : detail.getCosts()) {
                        if (cost.getCompanyId() == null) {
                            cost.setCompanyId(companyId);
                        }
                        cost.setPurchaseOrderDetail(detail);
                    }
                }
            }
        }
        
        return super.save(po);
    }

    @Transactional
    public PurchaseOrder createPurchaseOrder(PurchaseOrderRequest request) {
        PurchaseOrder po = new PurchaseOrder();
        po.setPoNo("PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        po.setPoDate(request.getPoDate());
        po.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        po.setRemarks(request.getRemarks());
        po.setCurrency(request.getCurrency());
        po.setExchangeRate(request.getExchangeRate());
        
        if (request.getOrderType() != null) {
            po.setOrderType(PurchaseOrder.OrderType.valueOf(request.getOrderType()));
        }

        if (request.getVendorId() != null) {
            po.setVendor(businessPartnerRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new RuntimeException("Vendor not found")));
        }

        if (request.getWarehouseId() != null) {
            po.setWarehouse(warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new RuntimeException("Warehouse not found")));
        }

        if (request.getLetterOfCreditId() != null) {
            po.setLetterOfCredit(letterOfCreditRepository.findById(request.getLetterOfCreditId())
                    .orElseThrow(() -> new RuntimeException("Letter of Credit not found")));
        }

        po.setStatus(PurchaseOrderStatus.DRAFT);
        
        BigDecimal subTotal = BigDecimal.ZERO;
        List<PurchaseOrderDetail> details = new ArrayList<>();
        
        if (request.getDetails() != null) {
            for (PurchaseOrderDetailRequest detailReq : request.getDetails()) {
                PurchaseOrderDetail detail = new PurchaseOrderDetail();
                detail.setPurchaseOrder(po);
                
                Product product = productRepository.findById(detailReq.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                detail.setProduct(product);
                detail.setOrderedQty(detailReq.getQty());
                detail.setUnitPrice(detailReq.getUnitPrice());
                
                BigDecimal lineTotal = detail.getOrderedQty().multiply(detail.getUnitPrice());
                detail.setTotalPrice(lineTotal);
                subTotal = subTotal.add(lineTotal);
                
                List<PurchaseOrderDetailCost> costs = new ArrayList<>();
                if (detailReq.getCosts() != null) {
                    for (PurchaseOrderDetailCostRequest costReq : detailReq.getCosts()) {
                        PurchaseOrderDetailCost cost = new PurchaseOrderDetailCost();
                        cost.setPurchaseOrderDetail(detail);
                        cost.setCostHead(costHeadRepository.findById(costReq.getCostHeadId())
                                .orElseThrow(() -> new RuntimeException("CostHead not found")));
                        cost.setAmount(costReq.getAmount());
                        cost.setIncludedInInventoryCost(costReq.getIncludedInInventoryCost());
                        costs.add(cost);
                    }
                }
                detail.setCosts(costs);
                details.add(detail);
            }
        }
        
        po.setDetails(details);
        
        po.setTaxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO);
        po.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        
        BigDecimal total = subTotal.add(po.getTaxAmount()).subtract(po.getDiscountAmount());
        po.setTotalAmount(total);

        po = this.save(po);

        // Start Workflow
        WorkflowStartRequest workflowReq = new WorkflowStartRequest();
        workflowReq.setWorkflowCode("PO_APPROVAL");
        workflowReq.setEntityId(po.getId());
        workflowReq.setEntityName("PurchaseOrder");
        workflowReq.setDocumentNo(po.getPoNo());
        workflowReq.setAmount(po.getTotalAmount());

        try {
            WorkflowInstance instance = workflowService.startWorkflow(workflowReq);
            po.setWorkflowInstanceId(instance.getId());
            po = this.save(po);
        } catch (Exception e) {
            System.err.println("Failed to start workflow: " + e.getMessage());
        }

        return po;
    }

    @Transactional
    public PurchaseOrder approvePurchaseOrder(Long id) {
        PurchaseOrder po = findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));
                
        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT purchase orders can be approved");
        }
        
        po.setStatus(PurchaseOrderStatus.APPROVED);
        return this.save(po);
    }
    
    @Transactional
    public PurchaseOrder receivePurchaseOrder(Long id, Long locationId) {
        PurchaseOrder po = findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));
                
        if (po.getStatus() != PurchaseOrderStatus.APPROVED && po.getStatus() != PurchaseOrderStatus.PARTIAL_RECEIVED) {
            throw new RuntimeException("Purchase Order must be APPROVED or PARTIAL_RECEIVED to receive stock");
        }
        
        boolean allReceived = true;
        
        for (PurchaseOrderDetail detail : po.getDetails()) {
            BigDecimal pendingQty = detail.getOrderedQty().subtract(detail.getReceivedQty());
            
            if (pendingQty.compareTo(BigDecimal.ZERO) > 0) {
                // Calculate Landed Cost
                BigDecimal totalLandedCost = detail.getTotalPrice(); // Base Price
                
                if (detail.getCosts() != null) {
                    for (PurchaseOrderDetailCost cost : detail.getCosts()) {
                        if (Boolean.TRUE.equals(cost.getIncludedInInventoryCost())) {
                            totalLandedCost = totalLandedCost.add(cost.getAmount());
                        }
                    }
                }
                
                // Final unit cost based on landed value
                BigDecimal landedUnitCost = totalLandedCost.divide(detail.getOrderedQty(), 6, java.math.RoundingMode.HALF_UP);
                
                // Execute Receipt via InventoryService
                InventoryTransactionRequest req = new InventoryTransactionRequest();
                req.setItemId(detail.getProduct().getId());
                req.setWarehouseId(po.getWarehouse().getId());
                req.setLocationId(locationId);
                req.setTransactionType("PO_RECEIPT");
                req.setDocumentType("PURCHASE_ORDER");
                req.setDocumentId(po.getId());
                req.setQuantity(pendingQty);
                req.setUnitCost(landedUnitCost);
                
                inventoryService.receiveStock(req);
                
                // Update detail
                detail.setReceivedQty(detail.getReceivedQty().add(pendingQty));
            } else {
                allReceived = allReceived && (detail.getOrderedQty().compareTo(detail.getReceivedQty()) == 0);
            }
        }
        
        po.setStatus(allReceived ? PurchaseOrderStatus.CLOSED : PurchaseOrderStatus.PARTIAL_RECEIVED);
        return this.save(po);
    }
    
    @Transactional
    public PurchaseOrder cancelPurchaseOrder(Long id) {
        PurchaseOrder po = findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Order not found"));
                
        if (po.getStatus() != PurchaseOrderStatus.DRAFT && po.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new RuntimeException("Only DRAFT or APPROVED purchase orders can be cancelled");
        }
        
        po.setStatus(PurchaseOrderStatus.CANCELLED);
        return this.save(po);
    }
    
    public List<PurchaseOrder> getPurchaseOrdersByLetterOfCreditId(Long lcId) {
        return purchaseOrderRepository.findByLetterOfCreditId(lcId);
    }
    
    @EventListener
    @Transactional
    public void handleWorkflowStatusChange(WorkflowStatusEvent event) {
        if ("PurchaseOrder".equals(event.getEntityName())) {
            purchaseOrderRepository.findById(event.getEntityId()).ifPresent(po -> {
                if ("APPROVED".equals(event.getStatus())) {
                    po.setStatus(PurchaseOrderStatus.APPROVED);
                } else if ("REJECTED".equals(event.getStatus())) {
                    po.setStatus(PurchaseOrderStatus.REJECTED);
                }
                purchaseOrderRepository.save(po);
            });
        }
    }
}
