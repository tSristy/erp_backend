package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.SerialNumberTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SerialNumberTransactionRepository extends JpaRepository<SerialNumberTransaction, Long>, JpaSpecificationExecutor<SerialNumberTransaction> {
}
