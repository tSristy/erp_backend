package org.enterprise.security.repository;

import org.enterprise.security.entity.Role;
import org.enterprise.security.entity.RolePermission;
import org.enterprise.security.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    // Find all permissions of a role
    List<RolePermission> findByRoleAndCompanyId(Role role, Long companyId);

    // Check duplicate assignment
    boolean existsByRoleAndPermission(Role role, Permission permission);

    // Get specific mapping
    Optional<RolePermission> findByRoleAndPermission(Role role, Permission permission);

    // Delete all permissions of a role (useful in role update screen)
    void deleteByRoleAndCompanyId(Role role, Long companyId);
}