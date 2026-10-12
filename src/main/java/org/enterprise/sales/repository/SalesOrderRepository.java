package org.enterprise.sales.repository;

import org.enterprise.sales.entity.SalesOrder;
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
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>, JpaSpecificationExecutor<SalesOrder> {
    boolean existsByOrderNo(String orderNo);
    Optional<SalesOrder> findTopByOrderByIdDesc();

    @Override
    @EntityGraph(attributePaths = {"customer", "warehouse", "salesQuotation"})
    Page<SalesOrder> findAll(Specification<SalesOrder> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"customer", "warehouse", "salesQuotation"})
    Page<SalesOrder> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"customer", "warehouse", "salesQuotation"})
    List<SalesOrder> findAll();

    @Override
    @EntityGraph(attributePaths = {"customer", "warehouse", "salesQuotation"})
    Optional<SalesOrder> findById(Long id);
}
