package org.enterprise.crm.sales.repository;

import org.enterprise.crm.sales.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    @Query("SELECT l FROM Lead l WHERE (:q IS NULL OR :q = '' OR " +
           "LOWER(COALESCE(l.firstName, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(l.lastName, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(l.email, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(l.phone, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(l.company, '')) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Lead> searchByQuery(@Param("q") String q, Pageable pageable);
}
