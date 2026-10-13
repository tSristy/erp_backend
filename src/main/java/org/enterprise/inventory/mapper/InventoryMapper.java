package org.enterprise.inventory.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import org.enterprise.inventory.entity.*;
import org.enterprise.inventory.dto.*;


@Mapper(componentModel = "spring")
public interface InventoryMapper {

    // ==================== Warehouse ====================

    @Mapping(target = "branch.id", source = "branchId")
    @Mapping(target = "inventoryAccount.id", source = "inventoryAccountId")
    @Mapping(target = "cogsAccount.id", source = "cogsAccountId")
    @Mapping(target = "salesRevenueAccount.id", source = "salesRevenueAccountId")
    @Mapping(target = "salesReturnAccount.id", source = "salesReturnAccountId")
    @Mapping(target = "salesDiscountAccount.id", source = "salesDiscountAccountId")
    @Mapping(target = "wipAccount.id", source = "wipAccountId")
    Warehouse toEntity(WarehouseDto dto);

    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "inventoryAccountId", source = "inventoryAccount.id")
    @Mapping(target = "inventoryAccountName", source = "inventoryAccount.name")
    @Mapping(target = "cogsAccountId", source = "cogsAccount.id")
    @Mapping(target = "cogsAccountName", source = "cogsAccount.name")
    @Mapping(target = "salesRevenueAccountId", source = "salesRevenueAccount.id")
    @Mapping(target = "salesRevenueAccountName", source = "salesRevenueAccount.name")
    @Mapping(target = "salesReturnAccountId", source = "salesReturnAccount.id")
    @Mapping(target = "salesReturnAccountName", source = "salesReturnAccount.name")
    @Mapping(target = "salesDiscountAccountId", source = "salesDiscountAccount.id")
    @Mapping(target = "salesDiscountAccountName", source = "salesDiscountAccount.name")
    @Mapping(target = "wipAccountId", source = "wipAccount.id")
    @Mapping(target = "wipAccountName", source = "wipAccount.name")
    WarehouseDto toDto(Warehouse entity);

    List<WarehouseDto> toDtoListWarehouse(List<Warehouse> entityList);


    // ==================== Location ====================

    @Mapping(target = "warehouse.id", source = "warehouseId")
    @Mapping(target = "parent.id", source = "parentId")
    Location toEntity(LocationDto dto);

    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    LocationDto toDto(Location entity);

    List<LocationDto> toDtoListLocation(List<Location> entityList);


}