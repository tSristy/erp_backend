package org.enterprise.reportengine.repository;

import org.enterprise.reportengine.entity.AclReportMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AclReportMasterRepository
        extends JpaRepository<AclReportMaster, Long> {

    Optional<AclReportMaster> findByCodeAndIsActive(
            String code,
            Boolean isActive
    );

    List<AclReportMaster> findByRptGroupAndIsActiveOrderBySortBy(
            String rptGroup,
            Boolean isActive
    );
}
