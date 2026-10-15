package org.enterprise.pos.restaurant.dto;

import lombok.Data;

@Data
public class RestaurantTableDto {
    private Long id;
    private String tableNumber;
    private String name;
    private Integer capacity;
    private String section;
    private String status;
    private Boolean active;
    private Integer displayOrder;
    private Long warehouseId;
    private String warehouseName;
}
