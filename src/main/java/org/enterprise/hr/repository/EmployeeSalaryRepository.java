package org.enterprise.hr.repository;

import org.enterprise.hr.entity.EmployeeSalary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface EmployeeSalaryRepository 
    extends JpaRepository<EmployeeSalary, Long>, JpaSpecificationExecutor<EmployeeSalary> {
        
    Optional<EmployeeSalary> findByEmployeeId(Long employeeId);
}
