package org.enterprise.production.repository;

import org.enterprise.production.entity.ManufacturingOrder;
import org.enterprise.sales.entity.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManufacturingOrderRepository extends JpaRepository<ManufacturingOrder, Long> {
@org.springframework.data.jpa.repository.Query("SELECT new org.enterprise.production.dto.DailyProductionReportDto(" +
            "o.orderDate, COUNT(o.id), SUM(o.plannedQuantity), SUM(o.producedQuantity)) " +
            "FROM ManufacturingOrder o " +
            "WHERE o.companyId = :companyId AND o.status IN :statuses " +
            "AND (cast(:startDate as date) IS NULL OR o.orderDate >= :startDate) " +
            "AND (cast(:endDate as date) IS NULL OR o.orderDate <= :endDate) " +
            "GROUP BY o.orderDate ORDER BY o.orderDate ASC")
    java.util.List<org.enterprise.production.dto.DailyProductionReportDto> getDailyProductionReport(
            @org.springframework.data.repository.query.Param("companyId") Long companyId,
            @org.springframework.data.repository.query.Param("statuses") java.util.List<ManufacturingOrder.OrderStatus> statuses,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT new org.enterprise.production.dto.ProductProductionReportDto(" +
            "o.finishedGood.id, o.finishedGood.sku, o.finishedGood.name, COUNT(o.id), SUM(o.plannedQuantity), SUM(o.producedQuantity)) " +
            "FROM ManufacturingOrder o " +
            "WHERE o.companyId = :companyId AND o.status IN :statuses " +
            "AND (cast(:startDate as date) IS NULL OR o.orderDate >= :startDate) " +
            "AND (cast(:endDate as date) IS NULL OR o.orderDate <= :endDate) " +
            "GROUP BY o.finishedGood.id, o.finishedGood.sku, o.finishedGood.name ORDER BY SUM(o.producedQuantity) DESC")
    java.util.List<org.enterprise.production.dto.ProductProductionReportDto> getProductProductionReport(
            @org.springframework.data.repository.query.Param("companyId") Long companyId,
            @org.springframework.data.repository.query.Param("statuses") java.util.List<ManufacturingOrder.OrderStatus> statuses,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT new org.enterprise.production.dto.ProductionStatusReportDto(" +
            "o.status, COUNT(o.id), SUM(o.plannedQuantity), SUM(o.producedQuantity)) " +
            "FROM ManufacturingOrder o " +
            "WHERE o.companyId = :companyId " +
            "AND (cast(:startDate as date) IS NULL OR o.orderDate >= :startDate) " +
            "AND (cast(:endDate as date) IS NULL OR o.orderDate <= :endDate) " +
            "GROUP BY o.status ORDER BY o.status ASC")
    java.util.List<org.enterprise.production.dto.ProductionStatusReportDto> getProductionStatusReport(
            @org.springframework.data.repository.query.Param("companyId") Long companyId,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT new org.enterprise.production.dto.ProductionYieldReportDto(" +
            "o.finishedGood.id, o.finishedGood.sku, o.finishedGood.name, SUM(o.plannedQuantity), SUM(o.producedQuantity), " +
            "(SUM(o.producedQuantity) / SUM(o.plannedQuantity)) * 100) " +
            "FROM ManufacturingOrder o " +
            "WHERE o.companyId = :companyId AND o.status = :status " +
            "AND (cast(:startDate as date) IS NULL OR o.orderDate >= :startDate) " +
            "AND (cast(:endDate as date) IS NULL OR o.orderDate <= :endDate) " +
            "GROUP BY o.finishedGood.id, o.finishedGood.sku, o.finishedGood.name " +
            "HAVING SUM(o.plannedQuantity) > 0 " +
            "ORDER BY ((SUM(o.producedQuantity) / SUM(o.plannedQuantity)) * 100) DESC")
    java.util.List<org.enterprise.production.dto.ProductionYieldReportDto> getProductionYieldReport(
            @org.springframework.data.repository.query.Param("companyId") Long companyId,
            @org.springframework.data.repository.query.Param("status") ManufacturingOrder.OrderStatus status,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT new org.enterprise.production.dto.BomUsageReportDto(" +
            "bi.rawMaterial.id, bi.rawMaterial.sku, bi.rawMaterial.name, " +
            "SUM((o.producedQuantity * bi.quantity) / o.bom.baseQuantity)) " +
            "FROM ManufacturingOrder o " +
            "JOIN o.bom.items bi " +
            "WHERE o.companyId = :companyId AND o.status IN :statuses AND o.bom.baseQuantity > 0 " +
            "AND (cast(:startDate as date) IS NULL OR o.orderDate >= :startDate) " +
            "AND (cast(:endDate as date) IS NULL OR o.orderDate <= :endDate) " +
            "GROUP BY bi.rawMaterial.id, bi.rawMaterial.sku, bi.rawMaterial.name " +
            "ORDER BY SUM((o.producedQuantity * bi.quantity) / o.bom.baseQuantity) DESC")
    java.util.List<org.enterprise.production.dto.BomUsageReportDto> getBomUsageReport(
            @org.springframework.data.repository.query.Param("companyId") Long companyId,
            @org.springframework.data.repository.query.Param("statuses") java.util.List<ManufacturingOrder.OrderStatus> statuses,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);
}
