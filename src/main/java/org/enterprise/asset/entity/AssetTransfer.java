package org.enterprise.asset.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;

import java.time.LocalDate;

@Entity
@Table(name = "fam_asset_transfers")
@Getter
@Setter
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class AssetTransfer extends AuditableEntity {

    @Column(nullable = false)
    private String transferNumber;

    @Column(nullable = false)
    private Long assetId;

    private String assetCode;
    private String assetName;

    @Column(nullable = false)
    private LocalDate transferDate;

    private Long fromEmployeeId;
    private String fromEmployeeName;

    private Long toEmployeeId;
    private String toEmployeeName;

    private Long fromDepartmentId;
    private String fromDepartmentName;

    private Long toDepartmentId;
    private String toDepartmentName;

    private String fromLocation;
    private String toLocation;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status = TransferStatus.COMPLETED;

    public enum TransferStatus {
        PENDING,
        COMPLETED,
        CANCELLED
    }
}
