package org.enterprise.pos.retail.repository;

import org.enterprise.pos.retail.entity.RetailTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetailTransactionRepository extends JpaRepository<RetailTransaction, Long> {
}
