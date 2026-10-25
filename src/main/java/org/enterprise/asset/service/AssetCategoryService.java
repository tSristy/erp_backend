package org.enterprise.asset.service;

import org.enterprise.asset.entity.AssetCategory;
import org.enterprise.asset.repository.AssetCategoryRepository;
import org.enterprise.common.util.TenantContext;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssetCategoryService extends BaseService<AssetCategory, Long> {

    private final AssetCategoryRepository categoryRepository;

    public AssetCategoryService(AssetCategoryRepository categoryRepository) {
        super(categoryRepository);
        this.categoryRepository = categoryRepository;
    }

    public List<AssetCategory> findAllByCurrentCompany() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId != null) {
            return categoryRepository.findByCompanyIdOrderByIdDesc(companyId);
        }
        return categoryRepository.findAll();
    }

    @Transactional
    @Override
    public AssetCategory save(AssetCategory entity) {
        if (entity.getCode() == null || entity.getCode().trim().isEmpty()) {
            entity.setCode("ACAT-" + (System.currentTimeMillis() % 100000));
        }
        return super.save(entity);
    }
}
