package org.enterprise.crm.sales.repository;

import org.enterprise.crm.sales.entity.Interaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    @Query("SELECT i FROM Interaction i WHERE i.opportunity.id = :opportunityId")
    List<Interaction> findByOpportunityId(@Param("opportunityId") Long opportunityId);

    @Query("SELECT i FROM Interaction i WHERE i.lead.id = :leadId")
    List<Interaction> findByLeadId(@Param("leadId") Long leadId);

    @Query("SELECT i FROM Interaction i LEFT JOIN i.opportunity o LEFT JOIN i.lead l WHERE " +
           "(:q IS NULL OR :q = '' OR " +
           "LOWER(COALESCE(i.notes, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(o.name, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(l.firstName, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(l.lastName, '')) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Interaction> searchByQuery(@Param("q") String q, Pageable pageable);
}
