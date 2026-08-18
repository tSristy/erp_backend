package org.enterprise.pos.restaurant.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class RestaurantOrderDto {
    private Long id;
    private String orderNo;
    private LocalDateTime orderDate;
    private String tableNumber;
    private Long waiterId;
    private String orderType;
    private LocalDateTime eventDateTime;
    private String type; // TransactionType
    private Long referenceOrderId;
    private Long customerId;
    private Long warehouseId;
    private String status;
    private BigDecimal totalAmount;

    private List<RestaurantOrderDetailDto> details = new ArrayList<>();
    private List<RestaurantPaymentDto> payments = new ArrayList<>();
    private List<KitchenOrderTicketDto> kots = new ArrayList<>();
}
