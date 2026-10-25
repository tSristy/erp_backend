package org.enterprise.finance.repository;

import org.enterprise.finance.entity.PaymentReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentReceiptRepository extends JpaRepository<PaymentReceipt, Long>, JpaSpecificationExecutor<PaymentReceipt> {
    List<PaymentReceipt> findByCompanyId(Long companyId);
    Optional<PaymentReceipt> findByReceiptNo(String receiptNo);
}
