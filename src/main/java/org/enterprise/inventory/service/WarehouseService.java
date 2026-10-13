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
    private final org.enterprise.organization.repository.BranchRepository branchRepository;
    private final org.enterprise.finance.repository.AccountRepository accountRepository;

    public WarehouseService(WarehouseRepository warehouseRepository,
                            org.enterprise.organization.repository.BranchRepository branchRepository,
                            org.enterprise.finance.repository.AccountRepository accountRepository) {
        super(warehouseRepository);
        this.warehouseRepository = warehouseRepository;
        this.branchRepository = branchRepository;
        this.accountRepository = accountRepository;
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
        if (entity.getId() != null && warehouseRepository.existsById(entity.getId())) {
            return update(entity.getId(), entity);
        }
        return create(entity);
    }

    @Transactional
    public Warehouse create(Warehouse entity) {
        resolveRelations(entity);
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

    @Transactional
    public Warehouse update(Long id, Warehouse incoming) {
        Warehouse existing = warehouseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + id));

        resolveRelations(incoming);

        if (incoming.getCode() != null && !incoming.getCode().trim().isEmpty()) {
            existing.setCode(incoming.getCode().trim());
        }
        if (incoming.getName() != null) {
            existing.setName(incoming.getName().trim());
        }
        existing.setShortName(incoming.getShortName());
        existing.setType(incoming.getType());
        existing.setWarehouseCategory(incoming.getWarehouseCategory());
        existing.setDisplayOrder(incoming.getDisplayOrder());
        if (incoming.getActive() != null) {
            existing.setActive(incoming.getActive());
        }
        existing.setDefaultWarehouse(incoming.getDefaultWarehouse());
        existing.setAllowNegativeStock(incoming.getAllowNegativeStock());
        existing.setBatchManaged(incoming.getBatchManaged());
        existing.setSerialManaged(incoming.getSerialManaged());
        existing.setBinManaged(incoming.getBinManaged());
        existing.setQuarantineEnabled(incoming.getQuarantineEnabled());
        existing.setContactPerson(incoming.getContactPerson());
        existing.setEmail(incoming.getEmail());
        existing.setPhone(incoming.getPhone());
        existing.setCountry(incoming.getCountry());
        existing.setCity(incoming.getCity());
        existing.setZipCode(incoming.getZipCode());
        existing.setAddress(incoming.getAddress());
        existing.setLatitude(incoming.getLatitude());
        existing.setLongitude(incoming.getLongitude());
        existing.setEffectiveFrom(incoming.getEffectiveFrom());
        existing.setEffectiveTo(incoming.getEffectiveTo());
        existing.setRemarks(incoming.getRemarks());

        existing.setBranch(incoming.getBranch());
        existing.setInventoryAccount(incoming.getInventoryAccount());
        existing.setCogsAccount(incoming.getCogsAccount());
        existing.setSalesRevenueAccount(incoming.getSalesRevenueAccount());
        existing.setSalesReturnAccount(incoming.getSalesReturnAccount());
        existing.setSalesDiscountAccount(incoming.getSalesDiscountAccount());
        existing.setWipAccount(incoming.getWipAccount());

        return warehouseRepository.save(existing);
    }

    private void resolveRelations(Warehouse entity) {
        if (entity.getBranch() != null && entity.getBranch().getId() != null) {
            entity.setBranch(branchRepository.findById(entity.getBranch().getId()).orElse(null));
        } else {
            entity.setBranch(null);
        }

        if (entity.getInventoryAccount() != null && entity.getInventoryAccount().getId() != null) {
            entity.setInventoryAccount(accountRepository.findById(entity.getInventoryAccount().getId()).orElse(null));
        } else {
            entity.setInventoryAccount(null);
        }

        if (entity.getCogsAccount() != null && entity.getCogsAccount().getId() != null) {
            entity.setCogsAccount(accountRepository.findById(entity.getCogsAccount().getId()).orElse(null));
        } else {
            entity.setCogsAccount(null);
        }

        if (entity.getSalesRevenueAccount() != null && entity.getSalesRevenueAccount().getId() != null) {
            entity.setSalesRevenueAccount(accountRepository.findById(entity.getSalesRevenueAccount().getId()).orElse(null));
        } else {
            entity.setSalesRevenueAccount(null);
        }

        if (entity.getSalesReturnAccount() != null && entity.getSalesReturnAccount().getId() != null) {
            entity.setSalesReturnAccount(accountRepository.findById(entity.getSalesReturnAccount().getId()).orElse(null));
        } else {
            entity.setSalesReturnAccount(null);
        }

        if (entity.getSalesDiscountAccount() != null && entity.getSalesDiscountAccount().getId() != null) {
            entity.setSalesDiscountAccount(accountRepository.findById(entity.getSalesDiscountAccount().getId()).orElse(null));
        } else {
            entity.setSalesDiscountAccount(null);
        }

        if (entity.getWipAccount() != null && entity.getWipAccount().getId() != null) {
            entity.setWipAccount(accountRepository.findById(entity.getWipAccount().getId()).orElse(null));
        } else {
            entity.setWipAccount(null);
        }
    }
}
