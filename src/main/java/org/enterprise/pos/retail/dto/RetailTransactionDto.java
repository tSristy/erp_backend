package org.enterprise.pos.retail.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.enterprise.common.dto.AuditableDto;
import org.enterprise.pos.retail.entity.RetailTransaction.TransactionStatus;
import org.enterprise.pos.retail.entity.RetailTransaction.TransactionType;

@Data
public class RetailTransactionDto extends AuditableDto {
    private String transactionNo;
    private java.time.LocalDate transactionDate;
    private TransactionType type;
    private TransactionStatus status;
    private Long customerId;
    private Long warehouseId;
    private Long referenceTransactionId;
    private BigDecimal subTotal;
    private BigDecimal taxTotal;
    private BigDecimal discountTotal;
    private BigDecimal grandTotal;
    private BigDecimal amountPaid;
    private BigDecimal amountDue;
    private List<RetailTransactionDetailDto> details;
    private List<RetailTransactionPaymentDto> payments;
}
