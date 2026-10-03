package org.enterprise.finance.repository;

import org.enterprise.finance.entity.PaymentVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentVoucherRepository extends JpaRepository<PaymentVoucher, Long>, JpaSpecificationExecutor<PaymentVoucher> {
    List<PaymentVoucher> findByCompanyId(Long companyId);
}
