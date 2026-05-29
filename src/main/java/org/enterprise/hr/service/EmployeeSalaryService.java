package org.enterprise.hr.service;

import org.enterprise.hr.dto.EmployeeSalaryDto;
import org.enterprise.hr.entity.EmployeeSalary;
import org.enterprise.hr.repository.EmployeeSalaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeSalaryService {

    private final EmployeeSalaryRepository repository;

    public EmployeeSalaryDto create(EmployeeSalaryDto dto) {
        EmployeeSalary entity = new EmployeeSalary();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public EmployeeSalaryDto update(Long id, EmployeeSalaryDto dto) {
        EmployeeSalary entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeSalary not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public EmployeeSalaryDto getById(Long id) {
        EmployeeSalary entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeSalary not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeSalaryDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(EmployeeSalaryDto dto, EmployeeSalary entity) {
        // TODO: Map relation employee manually from employeeId
        entity.setGrossSalary(dto.getGrossSalary());
    }

    private EmployeeSalaryDto mapEntityToDto(EmployeeSalary entity) {
        EmployeeSalaryDto dto = new EmployeeSalaryDto();
        dto.setId(entity.getId());
        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
        }
        dto.setGrossSalary(entity.getGrossSalary());
        return dto;
    }
}
