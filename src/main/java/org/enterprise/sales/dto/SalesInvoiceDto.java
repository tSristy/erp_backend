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
    private String deliveryNoteNo;
    private Long customerId;
    private String customerName;
    private Long warehouseId;
    private String warehouseName;
    private String warehouseVatNumber;
    private String warehouseAddress;
    private org.enterprise.sales.entity.SalesInvoice.SalesChannel salesChannel;
    private BigDecimal subTotal;
    private BigDecimal discountTotal;
    private BigDecimal vatTotal;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private InvoiceType invoiceType;
    private List<SalesInvoiceDiscountDto> discounts;
    private List<SalesInvoiceDetailDto> details;
}
