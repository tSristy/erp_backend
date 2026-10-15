package org.enterprise.pos.retail.repository;

import org.enterprise.pos.retail.entity.RetailTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RetailTransactionRepository extends JpaRepository<RetailTransaction, Long> {

    @Query("SELECT t FROM RetailTransaction t LEFT JOIN t.customer c WHERE " +
            "(:searchTerm IS NULL OR :searchTerm = '' OR " +
            "LOWER(t.transactionNo) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.walkInCustomerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.walkInCustomerPhone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<RetailTransaction> searchRetailTransactions(@Param("searchTerm") String searchTerm, Pageable pageable);
}
