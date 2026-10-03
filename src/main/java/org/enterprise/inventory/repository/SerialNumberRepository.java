package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.SerialNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SerialNumberRepository extends JpaRepository<SerialNumber, Long>, JpaSpecificationExecutor<SerialNumber> {
    java.util.Optional<SerialNumber> findBySerialNoAndProductId(String serialNo, Long productId);
}
