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


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<org.enterprise.inventory.entity.Warehouse> searchWarehouses(String query, org.springframework.data.domain.Pageable pageable) {
        return warehouseRepository.findAll(org.enterprise.inventory.specification.WarehouseSpecification.searchByQuery(query), pageable);
    }

    @Override
    @Transactional
    public Warehouse save(Warehouse entity) {
        if (entity.getCode() == null || entity.getCode().trim().isEmpty()) {
            warehouseRepository.findTopByCodeStartingWithOrderByIdDesc("WH-")
                .ifPresentOrElse(
                    lastWarehouse -> {
                        String lastCode = lastWarehouse.getCode();
                        try {
                            int seq = Integer.parseInt(lastCode.replace("WH-", ""));
                            entity.setCode(String.format("WH-%04d", seq + 1));
                        } catch (Exception e) {
                            entity.setCode("WH-0001");
                        }
                    },
                    () -> entity.setCode("WH-0001")
                );
        }
        return super.save(entity);
    }
}
