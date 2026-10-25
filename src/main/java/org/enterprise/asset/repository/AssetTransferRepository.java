package org.enterprise.asset.repository;

import org.enterprise.asset.entity.AssetTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetTransferRepository extends JpaRepository<AssetTransfer, Long> {
    List<AssetTransfer> findByCompanyIdOrderByIdDesc(Long companyId);
}
