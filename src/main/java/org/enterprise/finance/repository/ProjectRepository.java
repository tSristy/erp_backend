package org.enterprise.finance.repository;

import org.enterprise.finance.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p WHERE p.companyId = :companyId AND " +
           "(LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Project> searchByCompanyId(@Param("companyId") Long companyId, @Param("search") String search, Pageable pageable);

    Optional<Project> findByIdAndCompanyId(Long id, Long companyId);

    Optional<Project> findByCodeAndCompanyId(String code, Long companyId);
}
