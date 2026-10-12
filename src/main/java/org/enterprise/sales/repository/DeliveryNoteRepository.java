package org.enterprise.sales.repository;

import org.enterprise.sales.entity.DeliveryNote;
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
public interface DeliveryNoteRepository extends JpaRepository<DeliveryNote, Long>, JpaSpecificationExecutor<DeliveryNote> {
    boolean existsByDeliveryNo(String deliveryNo);
    Optional<DeliveryNote> findTopByOrderByIdDesc();

    @Override
    @EntityGraph(attributePaths = {"customer", "warehouse", "salesOrder", "details", "details.product", "details.variant", "details.salesOrderDetail", "details.salesOrderDetail.variant"})
    Optional<DeliveryNote> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"customer", "warehouse", "salesOrder"})
    Page<DeliveryNote> findAll(Specification<DeliveryNote> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"customer", "warehouse", "salesOrder"})
    Page<DeliveryNote> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"customer", "warehouse", "salesOrder"})
    List<DeliveryNote> findAll();
}
