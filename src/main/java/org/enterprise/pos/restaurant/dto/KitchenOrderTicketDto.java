package org.enterprise.pos.restaurant.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class KitchenOrderTicketDto {
    private Long id;
    private String kotNumber;
    private LocalDateTime sentTime;
    private String status;
    private Long orderId;
    private List<RestaurantOrderDetailDto> details = new ArrayList<>();
}
