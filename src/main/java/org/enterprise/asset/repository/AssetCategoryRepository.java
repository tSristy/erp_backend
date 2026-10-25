package org.enterprise.asset.repository;

import org.enterprise.asset.entity.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Long> {
    List<AssetCategory> findByCompanyIdOrderByIdDesc(Long companyId);
}
