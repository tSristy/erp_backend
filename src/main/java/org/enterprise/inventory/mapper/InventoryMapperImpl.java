package org.enterprise.inventory.mapper;

import org.enterprise.finance.entity.Account;
import org.enterprise.inventory.dto.LocationDto;
import org.enterprise.inventory.dto.WarehouseDto;
import org.enterprise.inventory.entity.Location;
import org.enterprise.inventory.entity.Warehouse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InventoryMapperImpl implements InventoryMapper {

    @Override
    public Warehouse toEntity(WarehouseDto dto) {
        if (dto == null) {
            return null;
        }

        Warehouse warehouse = new Warehouse();

        if (dto.getInventoryAccountId() != null) {
            Account inventoryAccount = new Account();
            inventoryAccount.setId(dto.getInventoryAccountId());
            warehouse.setInventoryAccount(inventoryAccount);
        }
        if (dto.getCogsAccountId() != null) {
            Account cogsAccount = new Account();
            cogsAccount.setId(dto.getCogsAccountId());
            warehouse.setCogsAccount(cogsAccount);
        }
        if (dto.getSalesRevenueAccountId() != null) {
            Account salesRevenueAccount = new Account();
            salesRevenueAccount.setId(dto.getSalesRevenueAccountId());
            warehouse.setSalesRevenueAccount(salesRevenueAccount);
        }
        if (dto.getSalesReturnAccountId() != null) {
            Account salesReturnAccount = new Account();
            salesReturnAccount.setId(dto.getSalesReturnAccountId());
            warehouse.setSalesReturnAccount(salesReturnAccount);
        }
        if (dto.getSalesDiscountAccountId() != null) {
            Account salesDiscountAccount = new Account();
            salesDiscountAccount.setId(dto.getSalesDiscountAccountId());
            warehouse.setSalesDiscountAccount(salesDiscountAccount);
        }

        warehouse.setId(dto.getId());
        warehouse.setCompanyId(dto.getCompanyId());
        warehouse.setCode(dto.getCode());
        warehouse.setName(dto.getName());
        warehouse.setAddress(dto.getAddress());
        
        if (dto.getLocations() != null) {
            warehouse.setLocations(toEntityListLocation(dto.getLocations()));
        }

        return warehouse;
    }

    @Override
    public WarehouseDto toDto(Warehouse entity) {
        if (entity == null) {
            return null;
        }

        WarehouseDto warehouseDto = new WarehouseDto();

        if (entity.getInventoryAccount() != null) {
            warehouseDto.setInventoryAccountId(entity.getInventoryAccount().getId());
        }
        if (entity.getCogsAccount() != null) {
            warehouseDto.setCogsAccountId(entity.getCogsAccount().getId());
        }
        if (entity.getSalesRevenueAccount() != null) {
            warehouseDto.setSalesRevenueAccountId(entity.getSalesRevenueAccount().getId());
        }
        if (entity.getSalesReturnAccount() != null) {
            warehouseDto.setSalesReturnAccountId(entity.getSalesReturnAccount().getId());
        }
        if (entity.getSalesDiscountAccount() != null) {
            warehouseDto.setSalesDiscountAccountId(entity.getSalesDiscountAccount().getId());
        }

        warehouseDto.setId(entity.getId());
        warehouseDto.setCompanyId(entity.getCompanyId());
        warehouseDto.setCode(entity.getCode());
        warehouseDto.setName(entity.getName());
        warehouseDto.setAddress(entity.getAddress());
        
        if (entity.getLocations() != null) {
            warehouseDto.setLocations(toDtoListLocation(entity.getLocations()));
        }

        return warehouseDto;
    }

    @Override
    public Location toEntity(LocationDto dto) {
        if (dto == null) {
            return null;
        }

        Location location = new Location();

        if (dto.getWarehouseId() != null) {
            Warehouse warehouse = new Warehouse();
            warehouse.setId(dto.getWarehouseId());
            location.setWarehouse(warehouse);
        }
        if (dto.getParentId() != null) {
            Location parent = new Location();
            parent.setId(dto.getParentId());
            location.setParent(parent);
        }

        location.setId(dto.getId());
        location.setCompanyId(dto.getCompanyId());
        location.setCode(dto.getCode());
        location.setName(dto.getName());
        location.setType(dto.getType());
        
        if (dto.getChildren() != null) {
            location.setChildren(toEntityListLocation(dto.getChildren()));
        }

        return location;
    }

    @Override
    public LocationDto toDto(Location entity) {
        if (entity == null) {
            return null;
        }

        LocationDto locationDto = new LocationDto();

        if (entity.getWarehouse() != null) {
            locationDto.setWarehouseId(entity.getWarehouse().getId());
        }
        if (entity.getParent() != null) {
            locationDto.setParentId(entity.getParent().getId());
        }

        locationDto.setId(entity.getId());
        locationDto.setCompanyId(entity.getCompanyId());
        locationDto.setCode(entity.getCode());
        locationDto.setName(entity.getName());
        locationDto.setType(entity.getType());
        
        if (entity.getChildren() != null) {
            locationDto.setChildren(toDtoListLocation(entity.getChildren()));
        }

        return locationDto;
    }

    @Override
    public List<WarehouseDto> toDtoListWarehouse(List<Warehouse> entityList) {
        if (entityList == null) {
            return null;
        }

        List<WarehouseDto> list = new ArrayList<>(entityList.size());
        for (Warehouse warehouse : entityList) {
            list.add(toDto(warehouse));
        }

        return list;
    }

    @Override
    public List<LocationDto> toDtoListLocation(List<Location> entityList) {
        if (entityList == null) {
            return null;
        }

        List<LocationDto> list = new ArrayList<>(entityList.size());
        for (Location location : entityList) {
            list.add(toDto(location));
        }

        return list;
    }

    private List<Location> toEntityListLocation(List<LocationDto> list) {
        if (list == null) {
            return null;
        }

        List<Location> entityList = new ArrayList<>(list.size());
        for (LocationDto dto : list) {
            entityList.add(toEntity(dto));
        }

        return entityList;
    }
}
