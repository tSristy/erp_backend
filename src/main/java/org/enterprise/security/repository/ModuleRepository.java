package org.enterprise.security.repository;

import org.enterprise.security.entity.Module;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findByCodeAndCompanyId(String code, Long companyId);

    @Query(value = """
        SELECT m
        FROM Module m
        WHERE (:companyId IS NULL OR m.companyId = :companyId)
        AND m.active = true
        AND (
            :query IS NULL OR :query = '' OR
            LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(m.code) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(m.category) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        ORDER BY m.displayOrder ASC
    """,
    countQuery = """
        SELECT COUNT(m)
        FROM Module m
        WHERE (:companyId IS NULL OR m.companyId = :companyId)
        AND m.active = true
        AND (
            :query IS NULL OR :query = '' OR
            LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(m.code) LIKE LOWER(CONCAT('%', :query, '%')) OR
            LOWER(m.category) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
    Page<Module> search(
            @Param("companyId") Long companyId,
            @Param("query") String query,
            Pageable pageable
    );
}
