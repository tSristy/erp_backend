package org.enterprise.organization.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.organization.dto.ZoneDto;
import org.enterprise.organization.entity.Zone;
import org.enterprise.organization.repository.ZoneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ZoneService {

    private final ZoneRepository repository;

    public ZoneDto create(ZoneDto dto) {
        Zone entity = new Zone();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public ZoneDto update(Long id, ZoneDto dto) {
        Zone entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public ZoneDto getById(Long id) {
        Zone entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zone not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<ZoneDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(ZoneDto dto, Zone entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setManagerName(dto.getManagerName());
        entity.setContactNo(dto.getContactNo());
        if (dto.getActive() != null) entity.setActive(dto.getActive());
    }

    private ZoneDto mapEntityToDto(Zone entity) {
        ZoneDto dto = new ZoneDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setManagerName(entity.getManagerName());
        dto.setContactNo(entity.getContactNo());
        dto.setActive(entity.getActive());
        return dto;
    }
}
