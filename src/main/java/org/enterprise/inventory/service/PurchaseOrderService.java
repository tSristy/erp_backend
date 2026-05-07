package org.enterprise.inventory.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.inventory.dto.PurchaseOrderRequest;
import org.enterprise.inventory.entity.PurchaseOrder;
import org.enterprise.inventory.repository.PurchaseOrderRepository;
import org.enterprise.workflow.dto.WorkflowStartRequest;
import org.enterprise.workflow.entity.WorkflowInstance;
import org.enterprise.workflow.service.WorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final WorkflowService workflowService;

    @Transactional
    public PurchaseOrder createPurchaseOrder(PurchaseOrderRequest request) {
        
        PurchaseOrder po = new PurchaseOrder();
        po.setPoNo("PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        po.setPoDate(request.getPoDate());
        po.setTotalAmount(BigDecimal.valueOf(1000)); // Default amount for demonstration
        
        // Save initially to get ID
        po = purchaseOrderRepository.save(po);

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
            po = purchaseOrderRepository.save(po);
        } catch (Exception e) {
            // Workflow might not be defined or other error
            System.err.println("Failed to start workflow: " + e.getMessage());
        }

        return po;
    }
}
