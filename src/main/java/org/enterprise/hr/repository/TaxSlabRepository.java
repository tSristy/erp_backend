package org.enterprise.hr.repository;

import org.enterprise.hr.entity.TaxSlab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TaxSlabRepository extends JpaRepository<TaxSlab, Long>, JpaSpecificationExecutor<TaxSlab> {
}
