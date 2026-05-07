package org.enterprise.production.repository;

import org.enterprise.production.entity.BillOfMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillOfMaterialRepository extends JpaRepository<BillOfMaterial, Long> {
}
