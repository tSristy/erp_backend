package org.enterprise.pos.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.common.util.TenantContext;
import org.enterprise.inventory.repository.WarehouseRepository;
import org.enterprise.pos.restaurant.entity.RestaurantTable;
import org.enterprise.pos.restaurant.repository.RestaurantTableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantTableService {

    private final RestaurantTableRepository repository;
    private final WarehouseRepository warehouseRepository;

    @Transactional(readOnly = true)
    public List<RestaurantTable> getTablesByOutlet(Long warehouseId) {
        Long companyId = TenantContext.getCompanyId();
        return repository.findActiveTables(companyId, warehouseId);
    }

    @Transactional(readOnly = true)
    public Optional<RestaurantTable> findById(Long id) {
        return repository.findByIdWithWarehouse(id);
    }

    @Transactional
    public RestaurantTable save(RestaurantTable table) {
        Long whId = null;
        if (table.getWarehouse() != null && table.getWarehouse().getId() != null) {
            whId = table.getWarehouse().getId();
        } else if (table.getWarehouseId() != null) {
            whId = table.getWarehouseId();
        }

        if (whId != null) {
            table.setWarehouse(warehouseRepository.findById(whId).orElse(null));
        }

        Long companyId = TenantContext.getCompanyId();
        if (table.getCompanyId() == null) {
            if (companyId != null) {
                table.setCompanyId(companyId);
            } else if (table.getWarehouse() != null && table.getWarehouse().getCompanyId() != null) {
                table.setCompanyId(table.getWarehouse().getCompanyId());
            } else {
                table.setCompanyId(1L);
            }
        }

        return repository.save(table);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantTable> search(Long warehouseId, String query, Pageable pageable) {
        Long companyId = TenantContext.getCompanyId();
        return repository.searchTables(companyId, warehouseId, query, pageable);
    }
}
