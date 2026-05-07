package org.enterprise.sales.repository;

import org.enterprise.sales.entity.SalesQuotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesQuotationRepository extends JpaRepository<SalesQuotation, Long> {}
