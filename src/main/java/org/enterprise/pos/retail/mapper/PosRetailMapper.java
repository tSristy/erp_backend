package org.enterprise.pos.retail.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import org.enterprise.pos.retail.entity.*;
import org.enterprise.pos.retail.dto.*;

@Mapper(componentModel = "spring")
public interface PosRetailMapper {

    @Mapping(target = "customer.id", source = "customerId")
    @Mapping(target = "warehouse.id", source = "warehouseId")
    @Mapping(target = "referenceTransaction.id", source = "referenceTransactionId")
    @Mapping(target = "discountAmount", expression = "java(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : dto.getDiscountTotal())")
    @Mapping(target = "totalAmount", expression = "java(dto.getTotalAmount() != null ? dto.getTotalAmount() : dto.getGrandTotal())")
    @Mapping(target = "walkInCustomerName", expression = "java(dto.getWalkInCustomerName() != null ? dto.getWalkInCustomerName() : dto.getCustomerName())")
    @Mapping(target = "walkInCustomerPhone", expression = "java(dto.getWalkInCustomerPhone() != null ? dto.getWalkInCustomerPhone() : (dto.getContactNo() != null ? dto.getContactNo() : dto.getCustomerPhone()))")
    RetailTransaction toEntity(RetailTransactionDto dto);
    
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "referenceTransactionId", source = "referenceTransaction.id")
    @Mapping(target = "discountTotal", source = "discountAmount")
    @Mapping(target = "discountAmount", source = "discountAmount")
    @Mapping(target = "grandTotal", source = "totalAmount")
    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "customerName", expression = "java(entity.getCustomer() != null ? entity.getCustomer().getName() : entity.getWalkInCustomerName())")
    @Mapping(target = "customerPhone", expression = "java(entity.getCustomer() != null ? entity.getCustomer().getPhone() : entity.getWalkInCustomerPhone())")
    @Mapping(target = "contactNo", expression = "java(entity.getCustomer() != null ? entity.getCustomer().getPhone() : entity.getWalkInCustomerPhone())")
    @Mapping(target = "walkInCustomerName", source = "walkInCustomerName")
    @Mapping(target = "walkInCustomerPhone", source = "walkInCustomerPhone")
    RetailTransactionDto toDto(RetailTransaction entity);

    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "product.id", source = "productId")
    @Mapping(target = "variant.id", source = "variantId")
    @Mapping(target = "discountAmount", expression = "java(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : dto.getDiscount())")
    RetailTransactionDetail toEntity(RetailTransactionDetailDto dto);
    
    @Mapping(target = "transactionId", source = "transaction.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "variantSku", source = "variant.sku")
    @Mapping(target = "discount", source = "discountAmount")
    @Mapping(target = "discountAmount", source = "discountAmount")
    RetailTransactionDetailDto toDto(RetailTransactionDetail entity);

    @Mapping(target = "transactionDetail", ignore = true)
    RetailTransactionDetailDiscount toEntity(RetailTransactionDetailDiscountDto dto);

    @Mapping(target = "transactionDetailId", source = "transactionDetail.id")
    RetailTransactionDetailDiscountDto toDto(RetailTransactionDetailDiscount entity);

    @Mapping(target = "transaction", ignore = true)
    RetailTransactionPayment toEntity(RetailTransactionPaymentDto dto);
    
    @Mapping(target = "transactionId", source = "transaction.id")
    RetailTransactionPaymentDto toDto(RetailTransactionPayment entity);

    List<RetailTransactionDto> toDtoListRetailTransaction(List<RetailTransaction> entityList);

    default java.time.LocalDateTime map(java.time.LocalDate value) {
        return value != null ? value.atStartOfDay() : null;
    }
    
    default java.time.LocalDate map(java.time.LocalDateTime value) {
        return value != null ? value.toLocalDate() : null;
    }
}
