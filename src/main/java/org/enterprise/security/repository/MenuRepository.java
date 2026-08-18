package org.enterprise.security.repository;

import org.enterprise.security.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByModuleRouteAndCompanyIdAndVisibleTrueOrderByDisplayOrderAsc(String moduleRoute, Long companyId);
    List<Menu> findByModuleCodeAndCompanyIdOrderByDisplayOrderAsc(String moduleCode, Long companyId);
    Optional<Menu> findByCodeAndCompanyId(String code, Long companyId);
}
