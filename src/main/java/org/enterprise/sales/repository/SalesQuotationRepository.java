package org.enterprise.sales.repository;

import org.enterprise.sales.entity.SalesQuotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesQuotationRepository extends JpaRepository<SalesQuotation, Long>, JpaSpecificationExecutor<SalesQuotation> {
    boolean existsByQuotationNo(String quotationNo);
    Optional<SalesQuotation> findTopByOrderByIdDesc();

    @Override
    @EntityGraph(attributePaths = {"customer", "warehouse"})
    Page<SalesQuotation> findAll(Specification<SalesQuotation> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"customer", "warehouse"})
    Page<SalesQuotation> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"customer", "warehouse"})
    List<SalesQuotation> findAll();

    @Override
    @EntityGraph(attributePaths = {"customer", "warehouse"})
    Optional<SalesQuotation> findById(Long id);
}
