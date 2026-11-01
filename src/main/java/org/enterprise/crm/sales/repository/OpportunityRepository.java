package org.enterprise.crm.sales.repository;

import org.enterprise.crm.sales.entity.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {
    @Query("SELECT o FROM Opportunity o WHERE o.lead.id = :leadId")
    List<Opportunity> findByLeadId(@Param("leadId") Long leadId);

    @Query("SELECT o FROM Opportunity o WHERE o.customer.id = :customerId")
    List<Opportunity> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT o FROM Opportunity o LEFT JOIN o.customer c LEFT JOIN o.lead l WHERE " +
           "(:q IS NULL OR :q = '' OR " +
           "LOWER(COALESCE(o.name, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(c.name, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(l.firstName, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(l.lastName, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(l.company, '')) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Opportunity> searchByQuery(@Param("q") String q, Pageable pageable);
}
