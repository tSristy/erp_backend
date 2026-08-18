package org.enterprise.pos.restaurant.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RestaurantOrderDetailDto {
    private Long id;
    private Long productId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private String status;
}
