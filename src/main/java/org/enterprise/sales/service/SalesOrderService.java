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

    @Transactional
    public SalesOrder save(SalesOrder salesOrder) {
        return salesOrderRepository.save(salesOrder);
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
        returnOrder.setStatus(SalesOrder.SalesOrderStatus.DRAFT);
        returnOrder.setOrderDate(LocalDate.now());

        List<SalesOrderDetail> returnDetails = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (SalesOrderDetail originalDetail : original.getDetails()) {
            BigDecimal returnableQty = originalDetail.getShippedQuantity().subtract(originalDetail.getReturnedQuantity());
            if (returnableQty.compareTo(BigDecimal.ZERO) > 0) {
                SalesOrderDetail returnDetail = new SalesOrderDetail();
                returnDetail.setSalesOrder(returnOrder);
                returnDetail.setProduct(originalDetail.getProduct());
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

        return salesOrderRepository.save(returnOrder);
    }
}
