package org.enterprise.production.service;

import org.enterprise.production.dto.ManufacturingOrderDTO;
import org.enterprise.production.entity.ManufacturingOrder;
import org.enterprise.production.repository.ManufacturingOrderRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManufacturingOrderService {

    private final ManufacturingOrderRepository repository;

    public ManufacturingOrderService(ManufacturingOrderRepository repository) {
        this.repository = repository;
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
        ManufacturingOrder entity = convertToEntity(dto);
        ManufacturingOrder saved = repository.save(entity);
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
