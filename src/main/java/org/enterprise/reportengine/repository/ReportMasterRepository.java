package org.enterprise.reportengine.repository;

import org.enterprise.reportengine.entity.ReportMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportMasterRepository
        extends JpaRepository<ReportMaster, Long> {

    Optional<ReportMaster> findByCodeAndIsActive(
            String code,
            Boolean isActive
    );

    List<ReportMaster> findByRptGroupAndIsActiveOrderBySortBy(
            String rptGroup,
            Boolean isActive
    );
}
