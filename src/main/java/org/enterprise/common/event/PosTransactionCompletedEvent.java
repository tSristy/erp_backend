package org.enterprise.common.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PosTransactionCompletedEvent extends ApplicationEvent {
    private final Long transactionId;
    private final Long companyId;
    private final String transactionNo;
    private final String transactionType; // SALES or RETURN
    private final BigDecimal totalAmount;
    private final LocalDateTime transactionDate;

    private final Long customerId;
    private final Long warehouseId;

    private final List<LineItemDto> lineItems;
    private final List<PaymentDto> payments;

    public PosTransactionCompletedEvent(Object source, 
                                        String transactionNo, 
                                        String transactionType,
                                        BigDecimal totalAmount, 
                                        LocalDateTime transactionDate,
                                        Long customerId,
                                        Long warehouseId,
                                        List<LineItemDto> lineItems,
                                        List<PaymentDto> payments) {
        this(source, null, null, transactionNo, transactionType, totalAmount, transactionDate, customerId, warehouseId, lineItems, payments);
    }

    public PosTransactionCompletedEvent(Object source,
                                        Long transactionId,
                                        Long companyId,
                                        String transactionNo,
                                        String transactionType,
                                        BigDecimal totalAmount,
                                        LocalDateTime transactionDate,
                                        Long customerId,
                                        Long warehouseId,
                                        List<LineItemDto> lineItems,
                                        List<PaymentDto> payments) {
        super(source);
        this.transactionId = transactionId;
        this.companyId = companyId;
        this.transactionNo = transactionNo;
        this.transactionType = transactionType;
        this.totalAmount = totalAmount;
        this.transactionDate = transactionDate;
        this.customerId = customerId;
        this.warehouseId = warehouseId;
        this.lineItems = lineItems;
        this.payments = payments;
    }

    public static PosTransactionCompletedEvent fromInterfaces(
            Object source, String transactionNo, String transactionType, BigDecimal totalAmount, 
            LocalDateTime transactionDate, Long customerId, Long warehouseId,
            List<? extends PosLineItem> lineItems, List<? extends PosPayment> payments) {
        return fromInterfaces(source, null, null, transactionNo, transactionType, totalAmount, transactionDate, customerId, warehouseId, lineItems, payments);
    }

    public static PosTransactionCompletedEvent fromInterfaces(
            Object source, Long transactionId, Long companyId,
            String transactionNo, String transactionType, BigDecimal totalAmount, 
            LocalDateTime transactionDate, Long customerId, Long warehouseId,
            List<? extends PosLineItem> lineItems, List<? extends PosPayment> payments) {
        
        List<LineItemDto> lineItemDtos = lineItems != null ? lineItems.stream().map(item -> LineItemDto.builder()
                .productId(item.getProductId())
                .variantId(item.getVariantId())
                .batchId(item.getBatchId())
                .batchNo(item.getBatchNo())
                .serialNo(item.getSerialNo())
                .serialNumbers(item.getSerialNumbers() != null ? List.copyOf(item.getSerialNumbers()) : List.of())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .discounts(item.getLineDiscounts() != null ? item.getLineDiscounts().stream().map(d -> DiscountDto.builder()
                        .discountName(d.getDiscountName())
                        .discountAmount(d.getDiscountAmount())
                        .build()).collect(Collectors.toList()) : null)
                .build()).collect(Collectors.toList()) : List.of();
                
        List<PaymentDto> paymentDtos = payments != null ? payments.stream().map(payment -> PaymentDto.builder()
                .paymentMode(payment.getPaymentModeName())
                .amount(payment.getAmount())
                .referenceNumber(payment.getReferenceNumber())
                .build()).collect(Collectors.toList()) : List.of();
                
        return new PosTransactionCompletedEvent(source, transactionId, companyId, transactionNo, transactionType, totalAmount, transactionDate, customerId, warehouseId, lineItemDtos, paymentDtos);
    }

    @Getter
    @Builder
    @lombok.AllArgsConstructor
    public static class LineItemDto {
        private final Long productId;
        private final Long variantId;
        private final Long batchId;
        private final String batchNo;
        private final String serialNo;
        private final List<String> serialNumbers;
        private final BigDecimal quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal lineTotal;
        private final List<DiscountDto> discounts;
    }

    @Getter
    @Builder
    @lombok.AllArgsConstructor
    public static class DiscountDto {
        private final String discountName;
        private final BigDecimal discountAmount;
    }

    @Getter
    @Builder
    @lombok.AllArgsConstructor
    public static class PaymentDto {
        private final String paymentMode; // CASH, CARD, MFS, DUE
        private final BigDecimal amount;
        private final String referenceNumber;
    }
}
