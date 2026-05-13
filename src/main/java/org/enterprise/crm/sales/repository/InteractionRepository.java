package org.enterprise.crm.sales.repository;

import org.enterprise.crm.sales.entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    List<Interaction> findByOpportunityId(Long opportunityId);
    List<Interaction> findByLeadId(Long leadId);
}
