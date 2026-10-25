package org.enterprise.production.service;

import org.enterprise.common.util.TenantContext;
import org.enterprise.production.dto.QualityInspectionDTO;
import org.enterprise.production.entity.ManufacturingOrder;
import org.enterprise.production.entity.QualityInspection;
import org.enterprise.production.repository.ManufacturingOrderRepository;
import org.enterprise.production.repository.QualityInspectionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QualityInspectionService {

    private final QualityInspectionRepository repository;
    private final ManufacturingOrderRepository manufacturingOrderRepository;

    public QualityInspectionService(
            QualityInspectionRepository repository,
            ManufacturingOrderRepository manufacturingOrderRepository) {
        this.repository = repository;
        this.manufacturingOrderRepository = manufacturingOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<QualityInspectionDTO> findAll() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QualityInspectionDTO findById(Long id) {
        return repository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<QualityInspectionDTO> findByManufacturingOrderId(Long manufacturingOrderId) {
        return repository.findByManufacturingOrderIdOrderByIdDesc(manufacturingOrderId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public String generateNextInspectionNo() {
        return repository.findTopByOrderByIdDesc()
                .map(last -> String.format("QC-%04d", last.getId() + 1))
                .orElse("QC-0001");
    }

    @Transactional
    public QualityInspectionDTO save(QualityInspectionDTO dto) {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new RuntimeException("No active company context");
        }

        if (dto.getManufacturingOrderId() == null) {
            throw new RuntimeException("Manufacturing Order is required for Quality Inspection");
        }

        ManufacturingOrder order = manufacturingOrderRepository.findById(dto.getManufacturingOrderId())
                .orElseThrow(() -> new RuntimeException("Manufacturing Order not found"));

        QualityInspection entity = dto.getId() != null
                ? repository.findById(dto.getId()).orElseGet(QualityInspection::new)
                : new QualityInspection();

        if (dto.getInspectionNo() == null || dto.getInspectionNo().trim().isEmpty()) {
            entity.setInspectionNo(entity.getInspectionNo() != null ? entity.getInspectionNo() : generateNextInspectionNo());
        } else {
            entity.setInspectionNo(dto.getInspectionNo().trim());
        }

        entity.setInspectionDate(dto.getInspectionDate() != null ? dto.getInspectionDate() : LocalDate.now());
        entity.setManufacturingOrder(order);
        entity.setInspectorId(dto.getInspectorId());

        BigDecimal inspected = Optional.ofNullable(dto.getInspectedQuantity()).orElse(BigDecimal.ZERO);
        BigDecimal passed = Optional.ofNullable(dto.getPassedQuantity()).orElse(BigDecimal.ZERO);
        BigDecimal rejected = Optional.ofNullable(dto.getRejectedQuantity()).orElse(BigDecimal.ZERO);
        BigDecimal rework = Optional.ofNullable(dto.getReworkQuantity()).orElse(BigDecimal.ZERO);

        if (passed.add(rejected).add(rework).compareTo(inspected) > 0 && inspected.compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Sum of Passed, Rejected, and Rework quantities cannot exceed Inspected Quantity");
        }

        entity.setInspectedQuantity(inspected);
        entity.setPassedQuantity(passed);
        entity.setRejectedQuantity(rejected);
        entity.setReworkQuantity(rework);

        ManufacturingOrder.QcStatus resolvedStatus = dto.getStatus();
        if (resolvedStatus == null) {
            if (inspected.compareTo(BigDecimal.ZERO) == 0) {
                resolvedStatus = ManufacturingOrder.QcStatus.PENDING;
            } else if (rejected.compareTo(BigDecimal.ZERO) == 0 && rework.compareTo(BigDecimal.ZERO) == 0 && passed.compareTo(BigDecimal.ZERO) > 0) {
                resolvedStatus = ManufacturingOrder.QcStatus.PASSED;
            } else if (passed.compareTo(BigDecimal.ZERO) > 0) {
                resolvedStatus = ManufacturingOrder.QcStatus.PARTIALLY_PASSED;
            } else {
                resolvedStatus = ManufacturingOrder.QcStatus.FAILED;
            }
        }
        entity.setStatus(resolvedStatus);
        entity.setDefectReason(dto.getDefectReason());
        entity.setRemarks(dto.getRemarks());
        entity.setCompanyId(companyId);

        QualityInspection saved = repository.save(entity);

        // Sync QC summary fields onto the ManufacturingOrder
        order.setQcStatus(saved.getStatus());
        order.setQcInspectorId(saved.getInspectorId());
        order.setQcPassedQuantity(saved.getPassedQuantity());
        order.setQcRejectedQuantity(saved.getRejectedQuantity());
        order.setQcNotes(saved.getRemarks() != null && !saved.getRemarks().isEmpty()
                ? saved.getRemarks()
                : saved.getDefectReason());
        manufacturingOrderRepository.save(order);

        return convertToDTO(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<QualityInspectionDTO> search(Long companyId, Pageable pageable) {
        if (companyId != null) {
            return repository.findByCompanyId(companyId, pageable).map(this::convertToDTO);
        }
        return repository.findAll(pageable).map(this::convertToDTO);
    }

    private QualityInspectionDTO convertToDTO(QualityInspection entity) {
        QualityInspectionDTO dto = new QualityInspectionDTO();
        BeanUtils.copyProperties(entity, dto);
        if (entity.getManufacturingOrder() != null) {
            ManufacturingOrder mo = entity.getManufacturingOrder();
            dto.setManufacturingOrderId(mo.getId());
            dto.setManufacturingOrderNo(mo.getOrderNo());
            dto.setPlannedQuantity(mo.getPlannedQuantity());
            if (mo.getFinishedGood() != null) {
                dto.setFinishedGoodId(mo.getFinishedGood().getId());
                dto.setFinishedGoodName(mo.getFinishedGood().getName());
            }
        }
        return dto;
    }
}
