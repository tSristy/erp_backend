package org.enterprise.common.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.reportengine.entity.ReportDetail;
import org.enterprise.reportengine.entity.ReportMaster;
import org.enterprise.reportengine.enums.ReportParamType;
import org.enterprise.reportengine.repository.ReportMasterRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Order(4)
@RequiredArgsConstructor
public class ReportMasterSeeder implements CommandLineRunner {

    private final ReportMasterRepository reportMasterRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Running Report Master Seeder...");

        if (reportMasterRepository.findByCodeAndIsActive("SYSTEM_USERS", true).isEmpty()) {
            ReportMaster report = new ReportMaster();
            report.setCode("SYSTEM_USERS");
            report.setTitle("System Users Report");
            report.setRptGroup("Administration");
            report.setRemarks("Displays a list of all active users in the system, filterable by active status.");
            report.setSqlQuery("SELECT u.username AS \"Username\", u.email AS \"Email\", u.active AS \"Is Active\" " +
                               "FROM users u " +
                               "WHERE (:p_active = 'ALL' OR (:p_active = 'Y' AND u.active = true) OR (:p_active = 'N' AND u.active = false)) " +
                               "ORDER BY u.username ASC");
            report.setColumnHeader("Username,Email,Is Active");
            report.setSortBy(1);
            report.setIsActive(true);

            List<ReportDetail> parameters = new ArrayList<>();
            ReportDetail activeParam = new ReportDetail();
            activeParam.setParamName("p_active");
            activeParam.setTitle("User Status");
            activeParam.setParamType(ReportParamType.SELECT);
            activeParam.setQueryList("SELECT 'ALL' as id, 'All Users' as name UNION SELECT 'Y' as id, 'Active Only' as name UNION SELECT 'N' as id, 'Inactive Only' as name");
            activeParam.setDefaultValue("ALL");
            activeParam.setSortBy(1);
            activeParam.setIsMandatory(true);
            activeParam.setIsActive(true);
            activeParam.setMultiple(false);
            
            parameters.add(activeParam);
            
            report.setParameters(parameters);
            
            reportMasterRepository.save(report);
            log.info("Seeded 'System Users' dynamic report.");
        }
    }
}
