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
        return dto;
    }

    private ManufacturingOrder convertToEntity(ManufacturingOrderDTO dto) {
        ManufacturingOrder entity = new ManufacturingOrder();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
