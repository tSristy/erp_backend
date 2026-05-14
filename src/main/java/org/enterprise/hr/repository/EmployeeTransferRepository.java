package org.enterprise.hr.repository;

import org.enterprise.hr.entity.EmployeeTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeTransferRepository 
    extends JpaRepository<EmployeeTransfer, Long>, JpaSpecificationExecutor<EmployeeTransfer> {
}
