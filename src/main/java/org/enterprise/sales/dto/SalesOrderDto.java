package org.enterprise.sales.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.enterprise.common.dto.AuditableDto;
import org.enterprise.sales.entity.SalesOrder.SalesOrderStatus;
import org.enterprise.sales.entity.SalesOrder.OrderType;

@Data
public class SalesOrderDto extends AuditableDto {
    private String orderNo;
    private LocalDate orderDate;
    private SalesOrderStatus status;
    private Long customerId;
    private String customerName;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal subTotal;
    private BigDecimal discountTotal;
    private BigDecimal totalAmount;
    private OrderType orderType;
    private Long referenceOrderId;
    private List<SalesOrderDiscountDto> discounts;
    private List<SalesOrderDetailDto> details;
}
