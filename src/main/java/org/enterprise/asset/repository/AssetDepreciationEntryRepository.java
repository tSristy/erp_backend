package org.enterprise.asset.repository;

import org.enterprise.asset.entity.AssetDepreciationEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetDepreciationEntryRepository extends JpaRepository<AssetDepreciationEntry, Long> {
    List<AssetDepreciationEntry> findByCompanyIdOrderByIdDesc(Long companyId);
    boolean existsByAssetIdAndPeriodDate(Long assetId, java.time.LocalDate periodDate);
}
