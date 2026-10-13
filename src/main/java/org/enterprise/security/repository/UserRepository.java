package org.enterprise.security.repository;

import org.enterprise.security.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // =========================================
    // LOGIN
    // =========================================

    Optional<User> findByUsername(String username);

    // =========================================
    // ACTIVE USERS
    // =========================================

    List<User> findByActiveTrue();

    // =========================================
    // LOAD USER WITH ROLES
    // =========================================

    @EntityGraph(attributePaths = {
            "roles",
            "roles.role",
            "roles.role.rolePermissions",
            "roles.role.rolePermissions.permission"
    })
    Optional<User> findWithRolesByUsername(String username);

    // =========================================
    // TENANT USERS
    // =========================================

    @Query("""
        SELECT DISTINCT u
        FROM User u
        JOIN u.companies uc
        WHERE uc.company.id = :companyId
        AND uc.active = true
        AND u.active = true
    """)
    List<User> findActiveUsersByCompanyId(
            @Param("companyId") Long companyId
    );

    @Query(value = """
        SELECT DISTINCT u
        FROM User u
        JOIN u.companies uc
        WHERE uc.company.id = :companyId
        AND uc.active = true
        AND u.active = true
        AND (
            :query IS NULL OR :query = '' OR
            LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(u.mobile) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """,
    countQuery = """
        SELECT COUNT(DISTINCT u)
        FROM User u
        JOIN u.companies uc
        WHERE uc.company.id = :companyId
        AND uc.active = true
        AND u.active = true
        AND (
            :query IS NULL OR :query = '' OR
            LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(u.mobile) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
    Page<User> searchActiveUsersByCompanyId(
            @Param("companyId") Long companyId,
            @Param("query") String query,
            Pageable pageable
    );

    @Query(value = """
        SELECT u
        FROM User u
        WHERE u.active = true
        AND (
            :query IS NULL OR :query = '' OR
            LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(u.mobile) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """,
    countQuery = """
        SELECT COUNT(u)
        FROM User u
        WHERE u.active = true
        AND (
            :query IS NULL OR :query = '' OR
            LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(u.mobile) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
    Page<User> searchActiveUsers(
            @Param("query") String query,
            Pageable pageable
    );

    // =========================================
    // USER + COMPANY VALIDATION
    // =========================================

    @Query("""
        SELECT DISTINCT u
        FROM User u
        JOIN u.companies uc
        WHERE u.username = :username
        AND uc.company.id = :companyId
        AND uc.active = true
        AND u.active = true
    """)
    Optional<User> findByUsernameAndCompany(
            @Param("username") String username,
            @Param("companyId") Long companyId
    );
}