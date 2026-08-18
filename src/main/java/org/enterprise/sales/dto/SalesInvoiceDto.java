package org.enterprise.sales.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.enterprise.common.dto.AuditableDto;
import org.enterprise.sales.entity.SalesInvoice.InvoiceStatus;
import org.enterprise.sales.entity.SalesInvoice.InvoiceType;

@Data
public class SalesInvoiceDto extends AuditableDto {
    private String invoiceNo;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private Long deliveryNoteId;
    private Long customerId;
    private Long warehouseId;
    private BigDecimal subTotal;
    private BigDecimal discountTotal;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private InvoiceType invoiceType;
    private List<SalesInvoiceDiscountDto> discounts;
    private List<SalesInvoiceDetailDto> details;
}
