package org.enterprise.production.service;

import org.enterprise.production.dto.BillOfMaterialDTO;
import org.enterprise.production.dto.BomItemDTO;
import org.enterprise.production.entity.BillOfMaterial;
import org.enterprise.production.entity.BomItem;
import org.enterprise.production.repository.BillOfMaterialRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillOfMaterialService {

    private final BillOfMaterialRepository repository;

    public BillOfMaterialService(BillOfMaterialRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<BillOfMaterialDTO> findAll() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BillOfMaterialDTO findById(Long id) {
        return repository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Transactional
    public BillOfMaterialDTO save(BillOfMaterialDTO dto) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (companyId == null) {
            throw new RuntimeException("No active company context");
        }

        BillOfMaterial entity = convertToEntity(dto);
        entity.setCompanyId(companyId);
        
        if (entity.getItems() != null) {
            for (BomItem item : entity.getItems()) {
                if (item.getCompanyId() == null) {
                    item.setCompanyId(companyId);
                }
            }
        }
        
        BillOfMaterial saved = repository.save(entity);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private BillOfMaterialDTO convertToDTO(BillOfMaterial entity) {
        BillOfMaterialDTO dto = new BillOfMaterialDTO();
        BeanUtils.copyProperties(entity, dto, "items");
        if (entity.getFinishedGood() != null) dto.setFinishedGoodId(entity.getFinishedGood().getId());
        if (entity.getRouting() != null) dto.setRoutingId(entity.getRouting().getId());
        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream().map(item -> {
                BomItemDTO itemDto = new BomItemDTO();
                BeanUtils.copyProperties(item, itemDto);
                if (item.getRawMaterial() != null) itemDto.setRawMaterialId(item.getRawMaterial().getId());
                return itemDto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private BillOfMaterial convertToEntity(BillOfMaterialDTO dto) {
        BillOfMaterial entity = new BillOfMaterial();
        BeanUtils.copyProperties(dto, entity, "items");
        if (dto.getFinishedGoodId() != null) {
            org.enterprise.inventory.entity.Product fg = new org.enterprise.inventory.entity.Product();
            fg.setId(dto.getFinishedGoodId());
            entity.setFinishedGood(fg);
        }
        if (dto.getRoutingId() != null) {
            org.enterprise.production.entity.Routing routing = new org.enterprise.production.entity.Routing();
            routing.setId(dto.getRoutingId());
            entity.setRouting(routing);
        }
        if (dto.getItems() != null) {
            entity.setItems(dto.getItems().stream().map(itemDto -> {
                BomItem item = new BomItem();
                BeanUtils.copyProperties(itemDto, item);
                item.setBom(entity);
                if (itemDto.getRawMaterialId() != null) {
                    org.enterprise.inventory.entity.Product rm = new org.enterprise.inventory.entity.Product();
                    rm.setId(itemDto.getRawMaterialId());
                    item.setRawMaterial(rm);
                }
                return item;
            }).collect(Collectors.toList()));
        }
        return entity;
    }
}
