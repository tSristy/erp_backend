package org.enterprise.hr.service;

import org.enterprise.hr.dto.EmployeeEducationDto;
import org.enterprise.hr.entity.EmployeeEducation;
import org.enterprise.hr.repository.EmployeeEducationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeEducationService {

    private final EmployeeEducationRepository repository;

    public EmployeeEducationDto create(EmployeeEducationDto dto) {
        EmployeeEducation entity = new EmployeeEducation();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public EmployeeEducationDto update(Long id, EmployeeEducationDto dto) {
        EmployeeEducation entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeEducation not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public EmployeeEducationDto getById(Long id) {
        EmployeeEducation entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeEducation not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeEducationDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(EmployeeEducationDto dto, EmployeeEducation entity) {
        // TODO: Map relation employee manually from employeeId
        entity.setDegreeName(dto.getDegreeName());
        entity.setInstitute(dto.getInstitute());
        entity.setPassingYear(dto.getPassingYear());
    }

    private EmployeeEducationDto mapEntityToDto(EmployeeEducation entity) {
        EmployeeEducationDto dto = new EmployeeEducationDto();
        dto.setId(entity.getId());
        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
        }
        dto.setDegreeName(entity.getDegreeName());
        dto.setInstitute(entity.getInstitute());
        dto.setPassingYear(entity.getPassingYear());
        return dto;
    }
}
