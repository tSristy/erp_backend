package org.enterprise.hr.service;

import org.enterprise.hr.dto.DesignationDto;
import org.enterprise.hr.entity.Designation;
import org.enterprise.hr.repository.DesignationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DesignationService {

    private final DesignationRepository repository;

    public DesignationDto create(DesignationDto dto) {
        Designation entity = new Designation();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public DesignationDto update(Long id, DesignationDto dto) {
        Designation entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Designation not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public DesignationDto getById(Long id) {
        Designation entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Designation not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<DesignationDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(DesignationDto dto, Designation entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setGrade(dto.getGrade());
    }

    private DesignationDto mapEntityToDto(Designation entity) {
        DesignationDto dto = new DesignationDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setGrade(entity.getGrade());
        return dto;
    }
}
