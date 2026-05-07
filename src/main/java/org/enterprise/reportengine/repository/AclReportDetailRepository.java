package org.enterprise.reportengine.repository;

import org.enterprise.reportengine.entity.AclReportDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AclReportDetailRepository
        extends JpaRepository<AclReportDetail, Long> {

    List<AclReportDetail> findByAclReportMaster_CodeAndIsActiveOrderBySortBy(
            String reportCode,
            Boolean isActive
    );

    Optional<AclReportDetail>
    findByAclReportMaster_CodeAndParamNameAndIsActive(
            String reportCode,
            String paramName,
            Boolean isActive
    );
}
