package org.enterprise.organization.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.organization.dto.TerritoryDto;
import org.enterprise.organization.entity.Territory;
import org.enterprise.organization.entity.Zone;
import org.enterprise.organization.repository.TerritoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TerritoryService {

    private final TerritoryRepository repository;

    public TerritoryDto create(TerritoryDto dto) {
        Territory entity = new Territory();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public TerritoryDto update(Long id, TerritoryDto dto) {
        Territory entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Territory not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public TerritoryDto getById(Long id) {
        Territory entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Territory not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<TerritoryDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(TerritoryDto dto, Territory entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setSalesType(dto.getSalesType());
        if (dto.getActive() != null) entity.setActive(dto.getActive());
        
        if (dto.getAreaId() != null) {
            org.enterprise.organization.entity.Area area = new org.enterprise.organization.entity.Area();
            area.setId(dto.getAreaId());
            entity.setArea(area);
        } else {
            entity.setArea(null);
        }

        if (dto.getTerritoryManagerId() != null) {
            org.enterprise.hr.entity.Employee manager = new org.enterprise.hr.entity.Employee();
            manager.setId(dto.getTerritoryManagerId());
            entity.setTerritoryManager(manager);
        } else {
            entity.setTerritoryManager(null);
        }
    }

    private TerritoryDto mapEntityToDto(Territory entity) {
        TerritoryDto dto = new TerritoryDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setSalesType(entity.getSalesType());
        dto.setActive(entity.getActive());
        
        if (entity.getArea() != null) {
            dto.setAreaId(entity.getArea().getId());
        }
        if (entity.getTerritoryManager() != null) {
            dto.setTerritoryManagerId(entity.getTerritoryManager().getId());
        }
        return dto;
    }
}
