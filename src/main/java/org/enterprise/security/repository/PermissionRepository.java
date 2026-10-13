package org.enterprise.security.repository;

import org.enterprise.security.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByCode(String code);

    @Query(value = """
        SELECT p
        FROM Permission p
        WHERE (
            :query IS NULL OR :query = '' OR
            LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(p.moduleCode) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(p.actionType) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """,
    countQuery = """
        SELECT COUNT(p)
        FROM Permission p
        WHERE (
            :query IS NULL OR :query = '' OR
            LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(p.moduleCode) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(p.actionType) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
    Page<Permission> search(
            @Param("query") String query,
            Pageable pageable
    );
}