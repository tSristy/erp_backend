package org.enterprise.production.service;

import org.enterprise.production.dto.ManufacturingOrderDTO;
import org.enterprise.production.entity.ManufacturingOrder;
import org.enterprise.production.repository.ManufacturingOrderRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import org.enterprise.common.event.ManufacturingOrderCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.time.LocalDateTime;

@Service
public class ManufacturingOrderService {

    private final ManufacturingOrderRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public ManufacturingOrderService(ManufacturingOrderRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<ManufacturingOrderDTO> findAll() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ManufacturingOrderDTO findById(Long id) {
        return repository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Transactional
    public ManufacturingOrderDTO save(ManufacturingOrderDTO dto) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (companyId == null) {
            throw new RuntimeException("No active company context");
        }

        boolean wasCompleted = false;
        if (dto.getId() != null) {
            ManufacturingOrder existing = repository.findById(dto.getId()).orElse(null);
            if (existing != null && existing.getStatus() != ManufacturingOrder.OrderStatus.COMPLETED && 
                dto.getStatus() == org.enterprise.production.entity.ManufacturingOrder.OrderStatus.COMPLETED) {
                wasCompleted = true;
            }
        } else if (dto.getStatus() == org.enterprise.production.entity.ManufacturingOrder.OrderStatus.COMPLETED) {
            wasCompleted = true;
        }

        ManufacturingOrder entity = convertToEntity(dto);
        entity.setCompanyId(companyId);
        ManufacturingOrder saved = repository.save(entity);
        
        if (wasCompleted && saved.getFinishedGood() != null && saved.getBom() != null && saved.getProductionWarehouse() != null) {
            ManufacturingOrderCompletedEvent event = new ManufacturingOrderCompletedEvent(
                    this,
                    saved.getOrderNo(),
                    saved.getFinishedGood().getId(),
                    saved.getProductionWarehouse().getId(),
                    saved.getProducedQuantity(),
                    saved.getBom().getId(),
                    LocalDateTime.now()
            );
            eventPublisher.publishEvent(event);
        }
        
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private ManufacturingOrderDTO convertToDTO(ManufacturingOrder entity) {
        ManufacturingOrderDTO dto = new ManufacturingOrderDTO();
        BeanUtils.copyProperties(entity, dto);
        if (entity.getFinishedGood() != null) dto.setFinishedGoodId(entity.getFinishedGood().getId());
        if (entity.getBom() != null) dto.setBomId(entity.getBom().getId());
        if (entity.getProductionWarehouse() != null) dto.setProductionWarehouseId(entity.getProductionWarehouse().getId());
        if (entity.getBatch() != null) dto.setBatchId(entity.getBatch().getId());
        if (entity.getRouting() != null) dto.setRoutingId(entity.getRouting().getId());
        return dto;
    }

    private ManufacturingOrder convertToEntity(ManufacturingOrderDTO dto) {
        ManufacturingOrder entity = new ManufacturingOrder();
        BeanUtils.copyProperties(dto, entity);
        if (dto.getFinishedGoodId() != null) {
            org.enterprise.inventory.entity.Product fg = new org.enterprise.inventory.entity.Product();
            fg.setId(dto.getFinishedGoodId());
            entity.setFinishedGood(fg);
        }
        if (dto.getBomId() != null) {
            org.enterprise.production.entity.BillOfMaterial bom = new org.enterprise.production.entity.BillOfMaterial();
            bom.setId(dto.getBomId());
            entity.setBom(bom);
        }
        if (dto.getProductionWarehouseId() != null) {
            org.enterprise.inventory.entity.Warehouse w = new org.enterprise.inventory.entity.Warehouse();
            w.setId(dto.getProductionWarehouseId());
            entity.setProductionWarehouse(w);
        }
        if (dto.getBatchId() != null) {
            org.enterprise.inventory.entity.Batch b = new org.enterprise.inventory.entity.Batch();
            b.setId(dto.getBatchId());
            entity.setBatch(b);
        }
        if (dto.getRoutingId() != null) {
            org.enterprise.production.entity.Routing routing = new org.enterprise.production.entity.Routing();
            routing.setId(dto.getRoutingId());
            entity.setRouting(routing);
        }
        return entity;
    }
}
