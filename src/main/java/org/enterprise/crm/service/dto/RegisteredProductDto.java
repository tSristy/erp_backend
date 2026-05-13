package org.enterprise.crm.service.dto;

import lombok.Data;
import org.enterprise.crm.service.entity.RegisteredProduct;

import java.time.LocalDate;
import java.util.Map;

@Data
public class RegisteredProductDto {
    private Long id;
    private Long productId;
    private Long customerId;
    private String serialNumber;
    private LocalDate purchaseDate;
    private LocalDate warrantyExpiryDate;
    private RegisteredProduct.RegisteredProductStatus status;
    private Map<String, String> attributes;
}
