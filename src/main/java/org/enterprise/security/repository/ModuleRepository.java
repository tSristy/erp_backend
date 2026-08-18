package org.enterprise.security.repository;

import org.enterprise.security.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findByCodeAndCompanyId(String code, Long companyId);
}
