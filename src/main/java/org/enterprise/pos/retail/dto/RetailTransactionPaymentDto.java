package org.enterprise.pos.retail.dto;

import lombok.Data;
import java.math.BigDecimal;
import org.enterprise.common.dto.AuditableDto;
import org.enterprise.pos.retail.entity.RetailTransactionPayment.PaymentMode;

@Data
public class RetailTransactionPaymentDto extends AuditableDto {
    private Long transactionId;
    private PaymentMode paymentMode;
    private BigDecimal amount;
    private String reference;
}
