package org.enterprise.finance.repository;

import org.enterprise.finance.entity.StatementSetup;
import org.enterprise.finance.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatementSetupRepository
        extends JpaRepository<StatementSetup, Long> {

    List<StatementSetup>
    findByReportTypeOrderBySerialNo(
            ReportType reportType
    );
}