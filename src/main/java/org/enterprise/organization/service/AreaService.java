package org.enterprise.organization.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.organization.dto.AreaDto;
import org.enterprise.organization.entity.Area;
import org.enterprise.organization.entity.Territory;
import org.enterprise.organization.repository.AreaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AreaService {

    private final AreaRepository repository;

    public AreaDto create(AreaDto dto) {
        Area entity = new Area();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public AreaDto update(Long id, AreaDto dto) {
        Area entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Area not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public AreaDto getById(Long id) {
        Area entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Area not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<AreaDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(AreaDto dto, Area entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setMarketType(dto.getMarketType());
        entity.setPriorityLevel(dto.getPriorityLevel());
        if (dto.getActive() != null) entity.setActive(dto.getActive());
        
        if (dto.getTerritoryId() != null) {
            Territory territory = new Territory();
            territory.setId(dto.getTerritoryId());
            entity.setTerritory(territory);
        } else {
            entity.setTerritory(null);
        }
    }

    private AreaDto mapEntityToDto(Area entity) {
        AreaDto dto = new AreaDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setMarketType(entity.getMarketType());
        dto.setPriorityLevel(entity.getPriorityLevel());
        dto.setActive(entity.getActive());
        
        if (entity.getTerritory() != null) {
            dto.setTerritoryId(entity.getTerritory().getId());
        }
        return dto;
    }
}
