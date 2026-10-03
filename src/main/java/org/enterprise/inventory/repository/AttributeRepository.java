package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface AttributeRepository extends JpaRepository<Attribute, Long>, JpaSpecificationExecutor<Attribute> {
    List<Attribute> findByCompanyId(Long companyId);
}
