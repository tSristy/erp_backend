package org.enterprise.crm.sales.repository;

import org.enterprise.crm.sales.entity.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {
    List<Opportunity> findByLeadId(Long leadId);
    List<Opportunity> findByCustomerId(Long customerId);
}
