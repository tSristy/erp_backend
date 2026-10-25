package org.enterprise.asset.repository;

import org.enterprise.asset.entity.AssetDisposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetDisposalRepository extends JpaRepository<AssetDisposal, Long> {
    List<AssetDisposal> findByCompanyIdOrderByIdDesc(Long companyId);
}
