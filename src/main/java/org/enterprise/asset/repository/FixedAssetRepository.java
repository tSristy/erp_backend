package org.enterprise.asset.repository;

import org.enterprise.asset.entity.FixedAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixedAssetRepository extends JpaRepository<FixedAsset, Long> {
    List<FixedAsset> findByCompanyIdOrderByIdDesc(Long companyId);
    boolean existsByGoodsReceiptIdAndProductIdAndSerialNumber(Long goodsReceiptId, Long productId, String serialNumber);
    long countByGoodsReceiptIdAndProductId(Long goodsReceiptId, Long productId);
}
