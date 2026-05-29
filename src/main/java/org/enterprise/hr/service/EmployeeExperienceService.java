package org.enterprise.hr.service;

import org.enterprise.hr.dto.EmployeeExperienceDto;
import org.enterprise.hr.entity.EmployeeExperience;
import org.enterprise.hr.repository.EmployeeExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeExperienceService {

    private final EmployeeExperienceRepository repository;

    public EmployeeExperienceDto create(EmployeeExperienceDto dto) {
        EmployeeExperience entity = new EmployeeExperience();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public EmployeeExperienceDto update(Long id, EmployeeExperienceDto dto) {
        EmployeeExperience entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeExperience not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public EmployeeExperienceDto getById(Long id) {
        EmployeeExperience entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeExperience not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeExperienceDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(EmployeeExperienceDto dto, EmployeeExperience entity) {
        // TODO: Map relation employee manually from employeeId
        entity.setCompanyName(dto.getCompanyName());
        entity.setDesignation(dto.getDesignation());
        entity.setFromDate(dto.getFromDate());
        entity.setToDate(dto.getToDate());
    }

    private EmployeeExperienceDto mapEntityToDto(EmployeeExperience entity) {
        EmployeeExperienceDto dto = new EmployeeExperienceDto();
        dto.setId(entity.getId());
        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
        }
        dto.setCompanyName(entity.getCompanyName());
        dto.setDesignation(entity.getDesignation());
        dto.setFromDate(entity.getFromDate());
        dto.setToDate(entity.getToDate());
        return dto;
    }
}
