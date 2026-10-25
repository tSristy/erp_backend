package org.enterprise.asset.service;

import org.enterprise.asset.entity.AssetTransfer;
import org.enterprise.asset.entity.FixedAsset;
import org.enterprise.asset.repository.AssetTransferRepository;
import org.enterprise.asset.repository.FixedAssetRepository;
import org.enterprise.common.util.TenantContext;
import org.enterprise.inventory.service.BaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AssetTransferService extends BaseService<AssetTransfer, Long> {

    private final AssetTransferRepository transferRepository;
    private final FixedAssetRepository assetRepository;

    public AssetTransferService(
            AssetTransferRepository transferRepository,
            FixedAssetRepository assetRepository) {
        super(transferRepository);
        this.transferRepository = transferRepository;
        this.assetRepository = assetRepository;
    }

    public List<AssetTransfer> findAllByCurrentCompany() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId != null) {
            return transferRepository.findByCompanyIdOrderByIdDesc(companyId);
        }
        return transferRepository.findAll();
    }

    @Transactional
    @Override
    public AssetTransfer save(AssetTransfer entity) {
        if (entity.getTransferNumber() == null || entity.getTransferNumber().trim().isEmpty()) {
            entity.setTransferNumber("ATR-" + (System.currentTimeMillis() % 1000000));
        }
        if (entity.getTransferDate() == null) {
            entity.setTransferDate(LocalDate.now());
        }

        if (entity.getAssetId() != null) {
            FixedAsset asset = assetRepository.findById(entity.getAssetId()).orElse(null);
            if (asset != null) {
                entity.setAssetCode(asset.getAssetCode());
                entity.setAssetName(asset.getName());
                if (entity.getFromEmployeeId() == null) {
                    entity.setFromEmployeeId(asset.getAssignedEmployeeId());
                    entity.setFromEmployeeName(asset.getAssignedEmployeeName());
                }
                if (entity.getFromDepartmentId() == null) {
                    entity.setFromDepartmentId(asset.getDepartmentId());
                    entity.setFromDepartmentName(asset.getDepartmentName());
                }
                if (entity.getFromLocation() == null || entity.getFromLocation().isEmpty()) {
                    entity.setFromLocation(asset.getLocationName());
                }

                if (entity.getStatus() == AssetTransfer.TransferStatus.COMPLETED) {
                    asset.setAssignedEmployeeId(entity.getToEmployeeId());
                    asset.setAssignedEmployeeName(entity.getToEmployeeName());
                    asset.setDepartmentId(entity.getToDepartmentId());
                    asset.setDepartmentName(entity.getToDepartmentName());
                    if (entity.getToLocation() != null && !entity.getToLocation().trim().isEmpty()) {
                        asset.setLocationName(entity.getToLocation());
                    }
                    assetRepository.save(asset);
                }
            }
        }

        return super.save(entity);
    }
}
