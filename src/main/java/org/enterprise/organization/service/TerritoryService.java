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
        entity.setManagerName(dto.getManagerName());
        entity.setContactNo(dto.getContactNo());
        entity.setSalesTarget(dto.getSalesTarget());
        if (dto.getActive() != null) entity.setActive(dto.getActive());
        
        if (dto.getZoneId() != null) {
            Zone zone = new Zone();
            zone.setId(dto.getZoneId());
            entity.setZone(zone);
        } else {
            entity.setZone(null);
        }
    }

    private TerritoryDto mapEntityToDto(Territory entity) {
        TerritoryDto dto = new TerritoryDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setSalesType(entity.getSalesType());
        dto.setManagerName(entity.getManagerName());
        dto.setContactNo(entity.getContactNo());
        dto.setSalesTarget(entity.getSalesTarget());
        dto.setActive(entity.getActive());
        
        if (entity.getZone() != null) {
            dto.setZoneId(entity.getZone().getId());
        }
        return dto;
    }
}
