package org.enterprise.inventory.service;

import org.enterprise.inventory.entity.Warehouse;
import org.enterprise.inventory.repository.WarehouseRepository;
import org.enterprise.inventory.specification.WarehouseSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseService extends BaseService<Warehouse, Long> {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        super(warehouseRepository);
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public Page<Warehouse> search(String query, Pageable pageable) {
        return warehouseRepository.findAll(WarehouseSpecification.searchByQuery(query), pageable);
    }
}
