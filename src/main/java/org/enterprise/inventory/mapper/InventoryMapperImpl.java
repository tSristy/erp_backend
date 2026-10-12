package org.enterprise.inventory.mapper;

import org.enterprise.finance.entity.Account;
import org.enterprise.organization.entity.Branch;
import org.enterprise.inventory.dto.LocationDto;
import org.enterprise.inventory.dto.WarehouseDto;
import org.enterprise.inventory.entity.Location;
import org.enterprise.inventory.entity.Warehouse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InventoryMapperImpl implements InventoryMapper {

    private Account resolveAccount(Object accountObj, Long accountId) {
        if (accountId != null) {
            Account acc = new Account();
            acc.setId(accountId);
            return acc;
        }
        if (accountObj == null) {
            return null;
        }
        if (accountObj instanceof Number) {
            Account acc = new Account();
            acc.setId(((Number) accountObj).longValue());
            return acc;
        }
        if (accountObj instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) accountObj;
            Object idVal = map.get("id");
            if (idVal instanceof Number) {
                Account acc = new Account();
                acc.setId(((Number) idVal).longValue());
                return acc;
            } else if (idVal instanceof String) {
                try {
                    Account acc = new Account();
                    acc.setId(Long.parseLong((String) idVal));
                    return acc;
                } catch (NumberFormatException ignored) {}
            }
        }
        if (accountObj instanceof String) {
            String str = ((String) accountObj).trim();
            if (str.isEmpty()) return null;
            try {
                Account acc = new Account();
                acc.setId(Long.parseLong(str));
                return acc;
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private Branch resolveBranch(Object branchObj, Long branchId) {
        if (branchId != null) {
            Branch b = new Branch();
            b.setId(branchId);
            return b;
        }
        if (branchObj == null) return null;
        if (branchObj instanceof Number) {
            Branch b = new Branch();
            b.setId(((Number) branchObj).longValue());
            return b;
        }
        if (branchObj instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) branchObj;
            Object idVal = map.get("id");
            if (idVal instanceof Number) {
                Branch b = new Branch();
                b.setId(((Number) idVal).longValue());
                return b;
            } else if (idVal instanceof String) {
                try {
                    Branch b = new Branch();
                    b.setId(Long.parseLong((String) idVal));
                    return b;
                } catch (NumberFormatException ignored) {}
            }
        }
        if (branchObj instanceof String) {
            String str = ((String) branchObj).trim();
            if (str.isEmpty()) return null;
            try {
                Branch b = new Branch();
                b.setId(Long.parseLong(str));
                return b;
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    @Override
    public Warehouse toEntity(WarehouseDto dto) {
        if (dto == null) {
            return null;
        }

        Warehouse warehouse = new Warehouse();

        warehouse.setId(dto.getId());
        warehouse.setCode(dto.getCode());
        warehouse.setName(dto.getName());
        warehouse.setShortName(dto.getShortName());
        warehouse.setType(dto.getType());
        warehouse.setWarehouseCategory(dto.getWarehouseCategory());
        warehouse.setDisplayOrder(dto.getDisplayOrder());
        warehouse.setActive(dto.getActive() != null ? dto.getActive() : true);
        warehouse.setDefaultWarehouse(dto.getDefaultWarehouse() != null ? dto.getDefaultWarehouse() : false);
        warehouse.setAllowNegativeStock(dto.getAllowNegativeStock() != null ? dto.getAllowNegativeStock() : false);
        warehouse.setBatchManaged(dto.getBatchManaged() != null ? dto.getBatchManaged() : false);
        warehouse.setSerialManaged(dto.getSerialManaged() != null ? dto.getSerialManaged() : false);
        warehouse.setBinManaged(dto.getBinManaged() != null ? dto.getBinManaged() : true);
        warehouse.setQuarantineEnabled(dto.getQuarantineEnabled() != null ? dto.getQuarantineEnabled() : false);
        warehouse.setContactPerson(dto.getContactPerson());
        warehouse.setEmail(dto.getEmail());
        warehouse.setPhone(dto.getPhone());
        warehouse.setCountry(dto.getCountry());
        warehouse.setCity(dto.getCity());
        warehouse.setZipCode(dto.getZipCode());
        warehouse.setAddress(dto.getAddress());
        warehouse.setLatitude(dto.getLatitude());
        warehouse.setLongitude(dto.getLongitude());
        warehouse.setEffectiveFrom(dto.getEffectiveFrom());
        warehouse.setEffectiveTo(dto.getEffectiveTo());
        warehouse.setRemarks(dto.getRemarks());

        warehouse.setBranch(resolveBranch(dto.getBranch(), dto.getBranchId()));
        warehouse.setInventoryAccount(resolveAccount(dto.getInventoryAccount(), dto.getInventoryAccountId()));
        warehouse.setCogsAccount(resolveAccount(dto.getCogsAccount(), dto.getCogsAccountId()));
        warehouse.setSalesRevenueAccount(resolveAccount(dto.getSalesRevenueAccount(), dto.getSalesRevenueAccountId()));
        warehouse.setSalesReturnAccount(resolveAccount(dto.getSalesReturnAccount(), dto.getSalesReturnAccountId()));
        warehouse.setSalesDiscountAccount(resolveAccount(dto.getSalesDiscountAccount(), dto.getSalesDiscountAccountId()));
        warehouse.setWipAccount(resolveAccount(dto.getWipAccount(), dto.getWipAccountId()));

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

        warehouseDto.setId(entity.getId());
        warehouseDto.setCode(entity.getCode());
        warehouseDto.setName(entity.getName());
        warehouseDto.setShortName(entity.getShortName());
        warehouseDto.setType(entity.getType());
        warehouseDto.setWarehouseCategory(entity.getWarehouseCategory());
        warehouseDto.setDisplayOrder(entity.getDisplayOrder());
        warehouseDto.setActive(entity.getActive());
        warehouseDto.setDefaultWarehouse(entity.getDefaultWarehouse());
        warehouseDto.setAllowNegativeStock(entity.getAllowNegativeStock());
        warehouseDto.setBatchManaged(entity.getBatchManaged());
        warehouseDto.setSerialManaged(entity.getSerialManaged());
        warehouseDto.setBinManaged(entity.getBinManaged());
        warehouseDto.setQuarantineEnabled(entity.getQuarantineEnabled());
        warehouseDto.setContactPerson(entity.getContactPerson());
        warehouseDto.setEmail(entity.getEmail());
        warehouseDto.setPhone(entity.getPhone());
        warehouseDto.setCountry(entity.getCountry());
        warehouseDto.setCity(entity.getCity());
        warehouseDto.setZipCode(entity.getZipCode());
        warehouseDto.setAddress(entity.getAddress());
        warehouseDto.setLatitude(entity.getLatitude());
        warehouseDto.setLongitude(entity.getLongitude());
        warehouseDto.setEffectiveFrom(entity.getEffectiveFrom());
        warehouseDto.setEffectiveTo(entity.getEffectiveTo());
        warehouseDto.setRemarks(entity.getRemarks());

        if (entity.getBranch() != null) {
            warehouseDto.setBranchId(entity.getBranch().getId());
            warehouseDto.setBranchName(entity.getBranch().getName());
        }

        if (entity.getInventoryAccount() != null) {
            warehouseDto.setInventoryAccountId(entity.getInventoryAccount().getId());
            warehouseDto.setInventoryAccountName(entity.getInventoryAccount().getName());
        }
        if (entity.getCogsAccount() != null) {
            warehouseDto.setCogsAccountId(entity.getCogsAccount().getId());
            warehouseDto.setCogsAccountName(entity.getCogsAccount().getName());
        }
        if (entity.getSalesRevenueAccount() != null) {
            warehouseDto.setSalesRevenueAccountId(entity.getSalesRevenueAccount().getId());
            warehouseDto.setSalesRevenueAccountName(entity.getSalesRevenueAccount().getName());
        }
        if (entity.getSalesReturnAccount() != null) {
            warehouseDto.setSalesReturnAccountId(entity.getSalesReturnAccount().getId());
            warehouseDto.setSalesReturnAccountName(entity.getSalesReturnAccount().getName());
        }
        if (entity.getSalesDiscountAccount() != null) {
            warehouseDto.setSalesDiscountAccountId(entity.getSalesDiscountAccount().getId());
            warehouseDto.setSalesDiscountAccountName(entity.getSalesDiscountAccount().getName());
        }
        if (entity.getWipAccount() != null) {
            warehouseDto.setWipAccountId(entity.getWipAccount().getId());
            warehouseDto.setWipAccountName(entity.getWipAccount().getName());
        }

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
        location.setCode(dto.getCode());
        location.setName(dto.getName());
        
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
            locationDto.setWarehouseName(entity.getWarehouse().getName());
        }
        if (entity.getParent() != null) {
            locationDto.setParentId(entity.getParent().getId());
            locationDto.setParentName(entity.getParent().getName());
        }

        locationDto.setId(entity.getId());
        locationDto.setCode(entity.getCode());
        locationDto.setName(entity.getName());
        
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
