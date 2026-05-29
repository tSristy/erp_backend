package org.enterprise.hr.service;

import org.enterprise.hr.dto.EmployeeConfirmationDto;
import org.enterprise.hr.entity.EmployeeConfirmation;
import org.enterprise.hr.repository.EmployeeConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeConfirmationService {

    private final EmployeeConfirmationRepository repository;

    public EmployeeConfirmationDto create(EmployeeConfirmationDto dto) {
        EmployeeConfirmation entity = new EmployeeConfirmation();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public EmployeeConfirmationDto update(Long id, EmployeeConfirmationDto dto) {
        EmployeeConfirmation entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeConfirmation not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public EmployeeConfirmationDto getById(Long id) {
        EmployeeConfirmation entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeConfirmation not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeConfirmationDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(EmployeeConfirmationDto dto, EmployeeConfirmation entity) {
        // TODO: Map relation employee manually from employeeId
        entity.setConfirmationDate(dto.getConfirmationDate());
        entity.setStatus(dto.getStatus());
        entity.setRemarks(dto.getRemarks());
    }

    private EmployeeConfirmationDto mapEntityToDto(EmployeeConfirmation entity) {
        EmployeeConfirmationDto dto = new EmployeeConfirmationDto();
        dto.setId(entity.getId());
        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
        }
        dto.setConfirmationDate(entity.getConfirmationDate());
        dto.setStatus(entity.getStatus());
        dto.setRemarks(entity.getRemarks());
        return dto;
    }
}
