package org.enterprise.pos.restaurant.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RestaurantPaymentDto {
    private Long id;
    private String paymentMode;
    private BigDecimal amount;
    private BigDecimal tipAmount;
    private String referenceNumber;
}
