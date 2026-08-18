package org.enterprise.reportengine.repository;

import org.enterprise.reportengine.entity.ReportDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportDetailRepository
        extends JpaRepository<ReportDetail, Long> {

    List<ReportDetail> findByReportMaster_CodeAndIsActiveOrderBySortBy(
            String reportCode,
            Boolean isActive
    );

    Optional<ReportDetail>
    findByReportMaster_CodeAndParamNameAndIsActive(
            String reportCode,
            String paramName,
            Boolean isActive
    );
}
