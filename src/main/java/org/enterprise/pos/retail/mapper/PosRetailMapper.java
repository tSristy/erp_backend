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
    RetailTransaction toEntity(RetailTransactionDto dto);
    
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "referenceTransactionId", source = "referenceTransaction.id")
    RetailTransactionDto toDto(RetailTransaction entity);

    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "product.id", source = "productId")
    RetailTransactionDetail toEntity(RetailTransactionDetailDto dto);
    
    @Mapping(target = "transactionId", source = "transaction.id")
    @Mapping(target = "productId", source = "product.id")
    RetailTransactionDetailDto toDto(RetailTransactionDetail entity);

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
