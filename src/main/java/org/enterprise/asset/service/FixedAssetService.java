package org.enterprise.asset.service;

import org.enterprise.asset.entity.FixedAsset;
import org.enterprise.asset.repository.FixedAssetRepository;
import org.enterprise.common.util.TenantContext;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FixedAssetService extends BaseService<FixedAsset, Long> {

    private final FixedAssetRepository assetRepository;

    public FixedAssetService(FixedAssetRepository assetRepository) {
        super(assetRepository);
        this.assetRepository = assetRepository;
    }

    public List<FixedAsset> findAllByCurrentCompany() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId != null) {
            return assetRepository.findByCompanyIdOrderByIdDesc(companyId);
        }
        return assetRepository.findAll();
    }

    @Transactional
    @Override
    public FixedAsset save(FixedAsset entity) {
        if (entity.getAssetCode() == null || entity.getAssetCode().trim().isEmpty()) {
            entity.setAssetCode("FA-" + (System.currentTimeMillis() % 1000000));
        }
        if (entity.getPurchaseDate() == null) {
            entity.setPurchaseDate(LocalDate.now());
        }
        if (entity.getInServiceDate() == null) {
            entity.setInServiceDate(entity.getPurchaseDate());
        }
        if (entity.getPurchaseCost() == null) {
            entity.setPurchaseCost(BigDecimal.ZERO);
        }
        if (entity.getSalvageValue() == null) {
            entity.setSalvageValue(BigDecimal.ZERO);
        }
        if (entity.getAccumulatedDepreciation() == null) {
            entity.setAccumulatedDepreciation(BigDecimal.ZERO);
        }
        BigDecimal bookVal = entity.getPurchaseCost().subtract(entity.getAccumulatedDepreciation());
        if (bookVal.compareTo(BigDecimal.ZERO) < 0) {
            bookVal = BigDecimal.ZERO;
        }
        entity.setCurrentBookValue(bookVal);
        return super.save(entity);
    }
}
