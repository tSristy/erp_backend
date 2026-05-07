package org.enterprise.security.repository;

import org.enterprise.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // 🔐 Find by role code (global fallback)
    Optional<Role> findByCode(String code);

    // 🏢 Tenant-aware role lookup (RECOMMENDED for ERP)
    Optional<Role> findByCodeAndCompanyId(String code, Long companyId);

    // 📦 Get all roles for a company (admin screens)
    List<Role> findByCompanyId(Long companyId);

    // 🔥 Active role fetch with permissions (avoid lazy loading in auth)
    @Query("""
        SELECT r FROM Role r
        LEFT JOIN FETCH r.rolePermissions rp
        LEFT JOIN FETCH rp.permission
        WHERE r.code = :code
        AND r.companyId = :companyId
    """)
    Optional<Role> findByCodeWithPermissions(
            @Param("code") String code,
            @Param("companyId") Long companyId
    );

    // 🔍 Check existence (used in seeder)
    boolean existsByCodeAndCompanyId(String code, Long companyId);
}