package org.enterprise.common.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PosTransactionCompletedEvent extends ApplicationEvent {
    private final String transactionNo;
    private final BigDecimal totalAmount;
    private final LocalDateTime transactionDate;
    
    private final Long customerId;
    private final Long warehouseId;
    
    private final List<LineItemDto> lineItems;
    private final List<PaymentDto> payments;

    public PosTransactionCompletedEvent(Object source, 
                                        String transactionNo, 
                                        BigDecimal totalAmount, 
                                        LocalDateTime transactionDate,
                                        Long customerId,
                                        Long warehouseId,
                                        List<LineItemDto> lineItems,
                                        List<PaymentDto> payments) {
        super(source);
        this.transactionNo = transactionNo;
        this.totalAmount = totalAmount;
        this.transactionDate = transactionDate;
        this.customerId = customerId;
        this.warehouseId = warehouseId;
        this.lineItems = lineItems;
        this.payments = payments;
    }

    @Getter
    @Builder
    public static class LineItemDto {
        private final Long productId;
        private final BigDecimal quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal lineTotal;
        private final List<DiscountDto> discounts;
    }

    @Getter
    @Builder
    public static class DiscountDto {
        private final String discountName;
        private final BigDecimal discountAmount;
    }

    @Getter
    @Builder
    public static class PaymentDto {
        private final String paymentMode; // CASH, CARD, MFS, DUE
        private final BigDecimal amount;
        private final String referenceNumber;
    }
}
