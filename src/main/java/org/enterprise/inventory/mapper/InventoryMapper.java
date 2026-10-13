package org.enterprise.inventory.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import org.enterprise.inventory.entity.*;
import org.enterprise.inventory.dto.*;
import org.enterprise.organization.dto.BranchDto;
import org.enterprise.organization.entity.Branch;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    // ==================== Warehouse ====================

    @Mapping(target = "inventoryAccount.id", source = "inventoryAccountId")
    @Mapping(target = "cogsAccount.id", source = "cogsAccountId")
    @Mapping(target = "salesRevenueAccount.id", source = "salesRevenueAccountId")
    @Mapping(target = "salesReturnAccount.id", source = "salesReturnAccountId")
    @Mapping(target = "salesDiscountAccount.id", source = "salesDiscountAccountId")
    @Mapping(target = "wipAccount.id", source = "wipAccountId")
    Warehouse toEntity(WarehouseDto dto);

    @Mapping(target = "inventoryAccountId", source = "inventoryAccount.id")
    @Mapping(target = "cogsAccountId", source = "cogsAccount.id")
    @Mapping(target = "salesRevenueAccountId", source = "salesRevenueAccount.id")
    @Mapping(target = "salesReturnAccountId", source = "salesReturnAccount.id")
    @Mapping(target = "salesDiscountAccountId", source = "salesDiscountAccount.id")
    @Mapping(target = "wipAccountId", source = "wipAccount.id")
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


    // ==================== Branch ====================

    @Mapping(target = "company.id", source = "companyId")
    Branch toEntity(BranchDto dto);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.name")
    BranchDto toDto(Branch entity);

    List<BranchDto> toDtoListBranch(List<Branch> entityList);
}