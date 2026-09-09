package org.enterprise.hr.repository;

import org.enterprise.hr.entity.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {
    java.util.List<LoanInstallment> findByLoan_Employee_IdAndStatus(Long employeeId, String status);
}
