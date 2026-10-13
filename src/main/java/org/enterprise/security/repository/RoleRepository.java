package org.enterprise.security.repository;

import org.enterprise.security.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = """
        SELECT r FROM Role r
        WHERE (:companyId IS NULL OR r.companyId IS NULL OR r.companyId = :companyId)
        AND (
            :query IS NULL OR :query = '' OR
            LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(r.code) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """,
    countQuery = """
        SELECT COUNT(r) FROM Role r
        WHERE (:companyId IS NULL OR r.companyId IS NULL OR r.companyId = :companyId)
        AND (
            :query IS NULL OR :query = '' OR
            LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(r.code) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
    Page<Role> search(
            @Param("companyId") Long companyId,
            @Param("query") String query,
            Pageable pageable
    );
}