package org.enterprise.inventory.repository;

import org.enterprise.inventory.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    List<Attribute> findByCompanyId(Long companyId);
}
