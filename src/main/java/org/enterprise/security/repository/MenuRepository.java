package org.enterprise.security.repository;

import org.enterprise.security.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByModuleRouteAndVisibleTrueOrderByDisplayOrderAsc(String moduleRoute);
}
