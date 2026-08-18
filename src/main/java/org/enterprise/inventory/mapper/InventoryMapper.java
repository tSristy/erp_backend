package org.enterprise.inventory.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import org.enterprise.inventory.entity.*;
import org.enterprise.inventory.dto.*;
import org.enterprise.finance.entity.Account;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "inventoryAccount.id", source = "inventoryAccountId")
    @Mapping(target = "cogsAccount.id", source = "cogsAccountId")
    @Mapping(target = "salesRevenueAccount.id", source = "salesRevenueAccountId")
    @Mapping(target = "salesReturnAccount.id", source = "salesReturnAccountId")
    @Mapping(target = "salesDiscountAccount.id", source = "salesDiscountAccountId")
    Warehouse toEntity(WarehouseDto dto);
    
    @Mapping(target = "inventoryAccountId", source = "inventoryAccount.id")
    @Mapping(target = "cogsAccountId", source = "cogsAccount.id")
    @Mapping(target = "salesRevenueAccountId", source = "salesRevenueAccount.id")
    @Mapping(target = "salesReturnAccountId", source = "salesReturnAccount.id")
    @Mapping(target = "salesDiscountAccountId", source = "salesDiscountAccount.id")
    WarehouseDto toDto(Warehouse entity);

    @Mapping(target = "warehouse.id", source = "warehouseId")
    @Mapping(target = "parent.id", source = "parentId")
    Location toEntity(LocationDto dto);
    
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "parentId", source = "parent.id")
    LocationDto toDto(Location entity);

    List<WarehouseDto> toDtoListWarehouse(List<Warehouse> entityList);
    List<LocationDto> toDtoListLocation(List<Location> entityList);
}
