package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.Tax;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaxRepository extends JpaRepository<Tax, Long> {
    List<Tax> findByCompanyId(Long companyId);
}
