package org.enterprise.hr.repository;

import java.util.Optional;

import org.enterprise.hr.entity.PayrollProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PayrollProcessRepository 
    extends JpaRepository<PayrollProcess, Long>, JpaSpecificationExecutor<PayrollProcess> {
        
    Optional<PayrollProcess> findByProcessYearAndProcessMonth(Integer year, Integer month);
}
